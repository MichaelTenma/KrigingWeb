package com.example.krigingweb.Service;

import com.example.krigingweb.Entity.ErrorEntity;
import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.NutrientEntity;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.NutrientFilter;
import com.example.krigingweb.Util.GeoUtil;
import com.example.krigingweb.Interpolation.Kriging.InterpolationTask;
import com.example.krigingweb.Interpolation.Kriging.Variogram.SphericalVariogram;
import com.example.krigingweb.Interpolation.Kriging.SemiCloud;
import com.example.krigingweb.Util.Tuple;
import jsat.classifiers.DataPoint;
import jsat.classifiers.DataPointPair;
import jsat.linear.DenseVector;
import jsat.regression.OrdinaryKriging;
import jsat.regression.RegressionDataSet;
import lombok.SneakyThrows;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InterpolationService {

    /**
     * @param samplePointEntityList 采样点
     * @param landEntityList 插值地块
     * @return
     */
    @SneakyThrows
    public List<LandEntity> interpolate(
        List<SamplePointEntity> samplePointEntityList,
        List<LandEntity> landEntityList, double cellSize
    ) {
        Map<SoilNutrientEnum, InterpolationTask> interpolationTaskMap = new HashMap<>(SoilNutrientEnum.values().length);

        for(SoilNutrientEnum soilNutrientEnum : SoilNutrientEnum.values()){
            /* 应该对采样点各个指标进行过滤 */
            System.out.println("\n" + soilNutrientEnum);
            List<SamplePointEntity> filterSamplePointEntityList = samplePointEntityList.stream()
                    .filter(NutrientFilter.get(soilNutrientEnum)).collect(Collectors.toList());

            RegressionDataSet[] regressionDataSetArray =
                    SamplePointService.samplePointToRegressionDataSet(filterSamplePointEntityList, soilNutrientEnum);

            RegressionDataSet trainRegressionDataSet = regressionDataSetArray[0];
            RegressionDataSet testRegressionDataSet = regressionDataSetArray[1];

            Tuple<OrdinaryKriging, SphericalVariogram> tuple = this.trainOrdinaryKriging(trainRegressionDataSet);
            OrdinaryKriging ordinaryKriging = tuple.first;
            SphericalVariogram sphericalVariogram = tuple.second;

            ErrorEntity testErrorEntity = new ErrorEntity(
                this.calError(testRegressionDataSet.getDPPList(), ordinaryKriging)
            );

            ErrorEntity trainErrorEntity = new ErrorEntity(
                this.calError(trainRegressionDataSet.getDPPList(), ordinaryKriging)
            );

            Method setSoilNutrientMethod =
                    LandEntity.class.getMethod("set" + soilNutrientEnum, NutrientEntity.class);
            interpolationTaskMap.put(soilNutrientEnum, new InterpolationTask(
                ordinaryKriging, testErrorEntity, trainErrorEntity, sphericalVariogram, setSoilNutrientMethod
            ));
        }

        for(LandEntity landEntity : landEntityList){
            this.interpolate(landEntity, interpolationTaskMap, cellSize);
        }

        return landEntityList;
    }

    private LandEntity interpolate(
        LandEntity landEntity, Map<SoilNutrientEnum, InterpolationTask> interpolationTaskMap, double cellSize
    ) throws InvocationTargetException, IllegalAccessException {
        final Geometry geometry = landEntity.getMultiPolygon();
        final Envelope envelope = geometry.getEnvelopeInternal();
        final double halfCellSize = cellSize / 2;
        final SoilNutrientEnum[] soilNutrientEnumArray = SoilNutrientEnum.values();

        /* 默认初始化为0.0，用于存储养分的插值结果 */
        double[] nutrientArray = new double[soilNutrientEnumArray.length];

        int sumNum = 0;
        for(double beginY = envelope.getMinY(); beginY < envelope.getMaxY(); beginY += cellSize){
            for(double beginX = envelope.getMinX(); beginX < envelope.getMaxX(); beginX += cellSize){
                Coordinate coordinate = new Coordinate(beginX + halfCellSize, beginY + halfCellSize);
                Point point = GeoUtil.geometryFactory.createPoint(coordinate);
                boolean isContain = geometry.isWithinDistance(point, halfCellSize);
                if(isContain){
                    /* 一个地块同时插值多个指标 */
                    for(int i = 0;i < nutrientArray.length;i++){
                        SoilNutrientEnum soilNutrientEnum = soilNutrientEnumArray[i];
                        InterpolationTask interpolationTask = interpolationTaskMap.get(soilNutrientEnum);
                        nutrientArray[i] += interpolationTask.getRegressor().regress(new DataPoint(
                            new DenseVector(new double[]{point.getX(), point.getY()})
                        ));
                    }
                    sumNum++;
                }
            }
        }
        for(int i = 0;i < nutrientArray.length;i++){
            nutrientArray[i] /= sumNum;
            SoilNutrientEnum soilNutrientEnum = soilNutrientEnumArray[i];
            InterpolationTask interpolationTask = interpolationTaskMap.get(soilNutrientEnum);
            interpolationTask.getSetNutrientMethod().invoke(
                landEntity, new NutrientEntity(nutrientArray[i], interpolationTask.getTestErrorEntity())
            );
        }
        return landEntity;
    }

    public Tuple<OrdinaryKriging, SphericalVariogram> trainOrdinaryKriging(RegressionDataSet trainRegressionDataSet){
        return this.trainOrdinaryKriging(trainRegressionDataSet, 100, 35000);
    }

    public Tuple<OrdinaryKriging, SphericalVariogram> trainOrdinaryKriging(
        RegressionDataSet trainRegressionDataSet, double lag, double lagDistance
    ){
        SphericalVariogram sphericalVariogram = new SphericalVariogram();
        SemiCloud<SphericalVariogram> semiCloud = new SemiCloud<>(
            trainRegressionDataSet, lag, lagDistance, sphericalVariogram
        );
        sphericalVariogram = semiCloud.trainVariogram();

        System.out.println(sphericalVariogram);
        System.out.println("RMSE: " + semiCloud.loss(sphericalVariogram));

        OrdinaryKriging ordinaryKriging = new OrdinaryKriging(sphericalVariogram);
        ordinaryKriging.train(trainRegressionDataSet);
        return new Tuple<>(ordinaryKriging, sphericalVariogram);
    }

    private List<Double> calError(List<DataPointPair<Double>> list, OrdinaryKriging ordinaryKriging){
        List<Double> errorList = new ArrayList<>(list.size());

        for(DataPointPair<Double> dataPointPair : list){
            double value = dataPointPair.getPair();
            double predictValue = ordinaryKriging.regress(dataPointPair.getDataPoint());
            errorList.add(predictValue - value);
        }
        return errorList;
    }
}

