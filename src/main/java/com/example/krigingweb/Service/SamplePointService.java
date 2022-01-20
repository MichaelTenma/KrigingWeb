package com.example.krigingweb.Service;

import com.example.krigingweb.Entity.RowMapper.SamplePointRowMapper;
import com.example.krigingweb.Entity.SamplePointEntity;
import jsat.classifiers.DataPoint;
import jsat.classifiers.DataPointPair;
import jsat.linear.DenseVector;
import jsat.regression.RegressionDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
                "where point_id != '8a23b556-7a75-4da8-b19a-152fb4c8dbe9' and " +
                "distance <= 5000 and N > 1 and N <= 250 order by random();";
        return this.jdbcTemplate.query(sql, new SamplePointRowMapper());
    }

    public RegressionDataSet[] getRegressionDataSet(){
        List<SamplePointEntity> samplePointEntityList = this.list();

        List<DataPointPair<Double>> trainList = new ArrayList<>((int) (samplePointEntityList.size() * 0.9));
        List<DataPointPair<Double>> testList = new ArrayList<>((int) (samplePointEntityList.size() * 0.1));

        int i = 0;
        for(SamplePointEntity samplePointEntity : samplePointEntityList){
            i++;
            DataPointPair<Double> dataPointPair = new DataPointPair<Double>(
                new DataPoint(
                        new DenseVector(new double[]{
                                samplePointEntity.getGeom().getX(), samplePointEntity.getGeom().getY()
                        })
                ),samplePointEntity.getN()
            );
            if(i <= samplePointEntityList.size() * 0.9){
                trainList.add(dataPointPair);
            }else{
                testList.add(dataPointPair);
            }

        }
        return new RegressionDataSet[]{
            RegressionDataSet.usingDPPList(trainList),
            RegressionDataSet.usingDPPList(testList),
        };
    }
}
