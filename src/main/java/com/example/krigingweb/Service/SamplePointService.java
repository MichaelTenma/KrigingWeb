package com.example.krigingweb.Service;

import com.example.krigingweb.Entity.RowMapper.SamplePointRowMapper;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Enum.SoilNutrientEnum;
import jsat.classifiers.DataPoint;
import jsat.classifiers.DataPointPair;
import jsat.linear.DenseVector;
import jsat.regression.RegressionDataSet;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;
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
                "where distance <= 5000 and xmc = '恩平市' order by random();";
        return this.jdbcTemplate.query(sql, new SamplePointRowMapper());
    }

    @SneakyThrows
    public static RegressionDataSet[] samplePointToRegressionDataSet(
        List<SamplePointEntity> samplePointEntityList, SoilNutrientEnum soilNutrientEnum
    ) {
        final double trainPercent = 0.9;
        Method getSoilNutrientMethod = SamplePointEntity.class.getMethod("get" + soilNutrientEnum);

        List<DataPointPair<Double>> trainList = new ArrayList<>((int) (samplePointEntityList.size() * trainPercent));
        List<DataPointPair<Double>> testList = new ArrayList<>(samplePointEntityList.size() - trainList.size());

        int i = 0;
        for(SamplePointEntity samplePointEntity : samplePointEntityList){
            i++;
            double nutrient = (Double) getSoilNutrientMethod.invoke(samplePointEntity);
            DataPointPair<Double> dataPointPair = new DataPointPair<Double>(
                new DataPoint(
                    new DenseVector(new double[]{
                        samplePointEntity.getGeom().getX(), samplePointEntity.getGeom().getY()
                    })
                ), nutrient
            );
            if(i <= samplePointEntityList.size() * trainPercent){
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
