package com.example.krigingweb.Entity.RowMapper;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Util.GeoUtil;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LandRowMapper implements RowMapper<LandEntity> {
    @Override
    public LandEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        LandEntity landEntity = null;
        try {
            MultiPolygon multiPolygon = (MultiPolygon) GeoUtil.wktReader.read(rs.getString("multiPolygon"));

            landEntity = new LandEntity(
                UUID.fromString(rs.getString("land_id")), multiPolygon
            );
        } catch (ParseException e) {
            System.out.println("ParseException");
        }
        return landEntity;
    }
}
