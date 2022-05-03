package com.example.krigingweb.Entity.RowMapper;

import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Util.GeoUtil;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class SamplePointRowMapper implements RowMapper<SamplePointEntity> {

    @Override
    public SamplePointEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        UUID point_id = (UUID)rs.getObject("point_id");
        byte[] pointBytes = rs.getBytes("geom");
        Point point = GeoUtil.toGeometry(pointBytes);
        return new SamplePointEntity(
            point_id, point,
            rs.getInt("time"), rs.getString("SMC"),
            rs.getString("DMC"), rs.getString("XMC"),
            rs.getString("YMC"), rs.getString("CMC"),
            rs.getDouble("pH"), rs.getDouble("OC"),
            rs.getDouble("N"), rs.getDouble("P"),
            rs.getDouble("K"), rs.getDouble("distance")
        );
    }
}
