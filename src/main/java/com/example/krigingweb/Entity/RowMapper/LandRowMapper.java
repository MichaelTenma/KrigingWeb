package com.example.krigingweb.Entity.RowMapper;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Interpolation.Core.Util.GeoUtil;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

@Component
public class LandRowMapper implements RowMapper<LandEntity> {
    @Override
    public LandEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        UUID land_id = (UUID)rs.getObject("land_id");
        byte[] geomBytes = rs.getBytes("geom");
        MultiPolygon multiPolygon = GeoUtil.toGeometry(geomBytes);
        return new LandEntity(land_id, multiPolygon);
    }
}
