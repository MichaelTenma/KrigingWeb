package com.example.krigingweb.Entity.RowMapper;

import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.GeoUtil;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SamplePointRowMapper implements RowMapper<SamplePointEntity> {

    @Override
    public SamplePointEntity mapRow(ResultSet rs, int rowNum) throws SQLException {

        SamplePointEntity samplePointEntity = null;
        try {
            Point point = (Point) GeoUtil.wktReader.read(rs.getString("point"));

            samplePointEntity = new SamplePointEntity(
                UUID.fromString(rs.getString("point_id")), point,
                rs.getInt("time"), rs.getString("SMC"),
                rs.getString("DMC"), rs.getString("XMC"),
                rs.getString("YMC"), rs.getString("CMC"),
                rs.getDouble("pH"), rs.getDouble("OC"),
                rs.getDouble("N"), rs.getDouble("P"),
                rs.getDouble("K"), rs.getDouble("distance")
            );
        } catch (ParseException e) {
            System.out.println("ParseException");
        }
        return samplePointEntity;
    }
}
