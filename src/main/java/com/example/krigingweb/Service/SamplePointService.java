package com.example.krigingweb.Service;

import com.example.krigingweb.Entity.RowMapper.SamplePointRowMapper;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Util.GeoUtil;
import com.example.krigingweb.Interpolation.Distributor.Core.Rectangle;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@ConditionalOnProperty(prefix = "distributor", name = "enable", havingValue = "true")
@Service
public class SamplePointService {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SamplePointService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SamplePointEntity> list(){
        String sql = "" +
                "select *, ST_AsText(geom) as point from sample_points " +
                "where distance <= 5000 and xmc = '恩平市' order by random();";
        return this.jdbcTemplate.query(sql, new SamplePointRowMapper());
    }

    public List<SamplePointEntity> list(Rectangle rectangle){
        String sql =
                "select *, ST_AsText(geom) as point from sample_points \n" +
                "where distance <= %f \n" +
                "and ST_Intersects(geom, ST_geomFromText('%s', %d));";
        sql = String.format(sql, GeoUtil.samplePointMaxDistance, rectangle, GeoUtil.srid);
        return this.jdbcTemplate.query(sql, new SamplePointRowMapper());
    }

    public static Geometry getConvexHull(List<SamplePointEntity> samplePointEntityList){
        Point[] points = new Point[samplePointEntityList.size()];
        for(int i = 0;i < samplePointEntityList.size();i++){
            SamplePointEntity samplePointEntity = samplePointEntityList.get(i);
            points[i] = samplePointEntity.getGeom();
        }
        MultiPoint multiPoint = GeoUtil.geometryFactory.createMultiPoint(points);
        return multiPoint.convexHull();
    }

}
