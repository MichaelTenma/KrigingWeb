package com.example.krigingweb.Service;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.RowMapper.LandRowMapper;
import com.example.krigingweb.Entity.RowMapper.SamplePointRowMapper;
import com.example.krigingweb.Entity.SamplePointEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LandService {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LandService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LandEntity> list(){
        String sql =
                "select *, ST_AsText(ST_transform(geom, 3857)) as multiPolygon from 耕地地力评价单元图 " +
                "where xmc = '恩平市' order by random() limit 10;";
        return this.jdbcTemplate.query(sql, new LandRowMapper());
    }

    /**
     * 随机获取一个未插值地块，同时按一定容差选取附近地块，以这片地块为中心选择至少500个点进行插值
     */

}
