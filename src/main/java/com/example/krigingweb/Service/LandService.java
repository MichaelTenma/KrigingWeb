package com.example.krigingweb.Service;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.RowMapper.LandRowMapper;
import com.example.krigingweb.Entity.RowMapper.SamplePointRowMapper;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Enum.InterpolatedStatusEnum;
import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.Core.RectangleSearcher;
import com.example.krigingweb.Interpolation.Core.Util.GeoUtil;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LandService {
    private final JdbcTemplate jdbcTemplate;
    private final LandRowMapper landRowMapper;
    private final SamplePointRowMapper samplePointRowMapper;

    @Autowired
    public LandService(
            JdbcTemplate jdbcTemplate,
            LandRowMapper landRowMapper, SamplePointRowMapper samplePointRowMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.landRowMapper = landRowMapper;
        this.samplePointRowMapper = samplePointRowMapper;
    }

//    public List<LandEntity> list(){
//        String sql =
//                "select *, ST_AsText(geom) as multiPolygon from 耕地地力评价单元图 " +
//                "where xmc = '恩平市' order by random() limit 100;";
//        return this.jdbcTemplate.query(sql, this.landRowMapper);
//    }

    public List<LandEntity> list(Geometry buffer){
        String sql =
            "select *, ST_AsText(geom) as multiPolygon from 耕地地力评价单元图 " +
            "where ST_Intersects(geom, ST_GeomFromText('%s'));";
        sql = String.format(sql, buffer);
        return this.jdbcTemplate.query(sql, this.landRowMapper);
    }

    public List<LandEntity> list(Geometry buffer, InterpolatedStatusEnum interpolatedStatusEnum){
        String sql =
                "select *, ST_AsText(geom) as multiPolygon from 耕地地力评价单元图 " +
                "where ST_Intersects(geom, ST_GeomFromText('%s')) " +
                "and interpolated_status = '%s';";
        sql = String.format(sql, buffer, interpolatedStatusEnum);
        return this.jdbcTemplate.query(sql, this.landRowMapper);
    }

    public UUID getRandomLand(RectangleSearcher.Rectangle rectangle){
        String sql =
                "select land_id from 耕地地力评价单元图 " +
                "where ST_Intersects(geom, ST_GeomFromText('%s', %d)) " +
                "and interpolated_status = '%s' limit 1;";
        sql = String.format(sql, rectangle.toString(), InterpolatedStatusEnum.UnStart);
//        System.out.println("getRandomLand: \n" + sql);

        return this.jdbcTemplate.queryForObject(
            sql, (rs, rowNum) -> UUID.fromString(rs.getString("land_id"))
        );
    }

    public Double predictBufferDistance(UUID landID, double distance, int pointsNum){
        String sql = "with land_buffer as (\n" +
                "\tSELECT ST_Buffer(geom, %f) as land \n" +
                "\tFROM 耕地地力评价单元图 \n" +
                "\tWHERE land_id = '%s'\n" +
                "),\n" +
                "predict_buffer as (\n" +
                "\tselect (case when num <= 0 then %f else sqrt((%d.0 / num) * area / 3.14159265) end) as bufferDistance \n" +
                "\tfrom (\n" +
                "\t\tselect count(sample_points.*) as num \n" +
                "\t\tfrom sample_points, land_buffer \n" +
                "\t\twhere ST_Intersects(land_buffer.land, geom) \n" +
                "and sample_points.distance < %f \n" +
                "\t) as t1, (\n" +
                "\t\tselect ST_area(land) as area from land_buffer\n" +
                "\t) as t2\n" +
                ")\n" +
                "select bufferDistance from predict_buffer;";
        sql = String.format(sql, distance, landID, distance, pointsNum, GeoUtil.samplePointMaxDistance);

        return this.jdbcTemplate.queryForObject(
            sql, (rs, rowNum) -> rs.getDouble("bufferDistance")
        );
    }

    private Integer countPoints(UUID landID, double distance, SoilNutrientEnum soilNutrientEnum){
        String sql = "with land_buffer as (\n" +
                "\tSELECT ST_Buffer(geom, %f) as land \n" +
                "\tFROM 耕地地力评价单元图 \n" +
                "\tWHERE land_id = '%s'\n" +
                ")\n" +
                "select count(sample_points.*) as num from sample_points, land_buffer \n" +
                "where ST_Intersects(land_buffer.land, geom)\n" +
                "and sample_points.distance < %f \n" +
                "and sample_points.%s > %f \n" +
                "and sample_points.%s < %f;";
        sql = String.format(
            sql, distance, landID,
            GeoUtil.samplePointMaxDistance,
            soilNutrientEnum, soilNutrientEnum.leftRange,
            soilNutrientEnum, soilNutrientEnum.rightRange
        );
        return this.jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getInt("num"));
    }

    public double calMaxDistance(UUID landID, double predictDistance, int pointsNum){
        /* 每个指标选择350个有效点 */
        double maxDistance = 0;/* 各指标的有效率相差不多，只取距离最大的指标以简化代码设计 */
        for(SoilNutrientEnum soilNutrientEnum : SoilNutrientEnum.values()){
            double testDistance = predictDistance;
            while(true){
                Integer num = this.countPoints(landID, testDistance, soilNutrientEnum);
                if(num < pointsNum){
                    /* 增加一公里 */
                    testDistance += 1000;
                }else{
                    break;
                }
            }
            if(testDistance > maxDistance){
                maxDistance = testDistance;
            }
        }
        return maxDistance;
    }

    public List<SamplePointEntity> getSamplePointEntityList(UUID landID, double distance){
        String sql =
                "with land_buffer as (\n" +
                "\tSELECT ST_Buffer(geom, %f) as land \n" +
                "\tFROM 耕地地力评价单元图 \n" +
                "\tWHERE land_id = '%s'\n" +
                ")\n" +
                "select sample_points.* from sample_points, land_buffer \n" +
                "where ST_Intersects(land_buffer.land, geom) \n" +
                "and sample_points.distance < %f;";
        sql = String.format(sql, distance, landID, GeoUtil.samplePointMaxDistance);
        return this.jdbcTemplate.query(sql, this.samplePointRowMapper);
    }


    public void updateLand(List<LandEntity> landEntityList){

    }

    /**
     * 探测是否还有需要进行插值的地块
     * @return True表示还有需要插值的地块
     */
    public boolean hasLandToInterpolate(){
        UUID landID = this.getRandomLand(RectangleSearcher.getExtent());
        return landID != null;
    }
}
