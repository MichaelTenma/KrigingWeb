package com.example.krigingweb.Service;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.RowMapper.LandRowMapper;
import com.example.krigingweb.Entity.RowMapper.SamplePointRowMapper;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Enum.InterpolatedStatusEnum;
import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.Basic.RectangleSearcher;
import com.example.krigingweb.Interpolation.Core.Util.GeoUtil;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
                "where ST_Intersects(geom, ST_GeomFromText('%s', %d)) " +
                "and interpolated_status = '%s';";
        sql = String.format(sql, buffer, GeoUtil.srid, interpolatedStatusEnum);
        return this.jdbcTemplate.query(sql, this.landRowMapper);
    }

    public void markPrepareInterpolated(List<LandEntity> landEntityList){
        StringBuilder sb = new StringBuilder();
        landEntityList.forEach(landEntity -> {
            sb.append("'").append(landEntity.getLandId()).append("',");
        });
        sb.setCharAt(sb.length()-1, ' ');

        String sql = "UPDATE 耕地地力评价单元图 SET interpolated_status = '%s' WHERE land_id IN (%s) ;";
        sql = String.format(sql, InterpolatedStatusEnum.Prepare, sb.toString());
        this.jdbcTemplate.update(sql);
    }
    public UUID getRandomLand(RectangleSearcher.Rectangle rectangle){
        String sql =
                "select land_id from 耕地地力评价单元图 " +
                "where ST_Intersects(geom, ST_GeomFromText('%s', %d)) " +
                "and interpolated_status = '%s' limit 1;";
        sql = String.format(sql, rectangle.toString(), GeoUtil.srid, InterpolatedStatusEnum.UnStart);
//        System.out.println("getRandomLand: \n" + sql);

        List<UUID> landIDList = this.jdbcTemplate.query(
            sql, (rs, rowNum) -> (UUID)rs.getObject("land_id")
        );
        return landIDList.isEmpty() ? null : landIDList.get(0);
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
                    /* 增加300米 */
                    testDistance += 300;
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
                "select sample_points.*, ST_AsText(geom) as point from sample_points, land_buffer \n" +
                "where ST_Intersects(land_buffer.land, geom) \n" +
                "and sample_points.distance < %f;";
        sql = String.format(sql, distance, landID, GeoUtil.samplePointMaxDistance);
        return this.jdbcTemplate.query(sql, this.samplePointRowMapper);
    }


    public void updateLand(List<LandEntity> landEntityList){
        final String templateValues = "(%f, %f, %f, %f, %f, '%s', '%s'::UUID),";
        StringBuilder sb = new StringBuilder();
        landEntityList.forEach(landEntity -> {
            if(!landEntity.couldBeUpdate()) return;
            sb.append(String.format(
                templateValues,
                landEntity.getN().getNutrient(),
                landEntity.getP().getNutrient(),
                landEntity.getK().getNutrient(),
                landEntity.getOC().getNutrient(),
                landEntity.getPH().getNutrient(),
                InterpolatedStatusEnum.Done.toString(),
                landEntity.getLandId()
            ));
        });
        sb.setCharAt(sb.length()-1, ' ');

        String sql =
            "update 耕地地力评价单元图 set\n" +
                "\t碱解氮 = update_tbl.碱解氮,\n" +
                "\t有效磷 = update_tbl.有效磷,\n" +
                "\t速效钾 = update_tbl.速效钾,\n" +
                "\t有机质 = update_tbl.有机质,\n" +
                "\tpH = update_tbl.pH,\n" +
                "\tinterpolated_status = update_tbl.interpolated_status\n" +
            "from ( values " + sb +") as update_tbl(碱解氮, 有效磷, 速效钾, 有机质, pH, interpolated_status, land_id) \n" +
            "where 耕地地力评价单元图.land_id = update_tbl.land_id; ";
        this.jdbcTemplate.update(sql);
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
