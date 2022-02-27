package com.example.krigingweb.Interpolation.Core.Util;

import com.example.krigingweb.Entity.ErrorEntity;
import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.NutrientEntity;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.Core.Kriging.FixOrdinaryKriging;
import com.example.krigingweb.Entity.NutrientFilter;
import com.example.krigingweb.Service.SamplePointService;
import com.example.krigingweb.Interpolation.Core.Kriging.InterpolationTask;
import com.example.krigingweb.Interpolation.Core.Kriging.Variogram.SphericalVariogram;
import com.example.krigingweb.Interpolation.Core.Kriging.SemiCloud;
import jsat.linear.DenseVector;
import jsat.linear.Vec;
import jsat.regression.RegressionDataSet;
import jsat.regression.Regressor;
import org.locationtech.jts.geom.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InterpolaterUtil {

    /**
     * @param samplePointEntityList 采样点
     * @param landEntityList 插值地块
     * @param cellSize 插值精度（米）
     * @return 插值后的地块
     */
    public static List<LandEntity> interpolate(
        List<SamplePointEntity> samplePointEntityList,
        List<LandEntity> landEntityList, double cellSize
    ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<SoilNutrientEnum, InterpolationTask> interpolationTaskMap = new HashMap<>(SoilNutrientEnum.values().length);

        SoilNutrientEnum[] soilNutrientEnumArray = SoilNutrientEnum.values();
        for(SoilNutrientEnum soilNutrientEnum : soilNutrientEnumArray){
            /* 应该对采样点各个指标进行过滤 */
            System.out.println("\n" + soilNutrientEnum);
            List<SamplePointEntity> filterSamplePointEntityList = samplePointEntityList.stream()
                    .filter(NutrientFilter.get(soilNutrientEnum)).collect(Collectors.toList());

            Vec originalPoint = null;
            {
                SamplePointEntity samplePointEntity =
                        filterSamplePointEntityList != null ? filterSamplePointEntityList.get(0) : null;

                if(samplePointEntity != null){
                    Point point = samplePointEntity.getGeom();
                    originalPoint = new DenseVector(new double[]{
                        point.getX(), point.getY()
                    });
                }
            }

            RegressionDataSet[] regressionDataSetArray =
                    SamplePointService.samplePointToRegressionDataSet(filterSamplePointEntityList, soilNutrientEnum);

            RegressionDataSet trainRegressionDataSet = regressionDataSetArray[0];
            RegressionDataSet testRegressionDataSet = regressionDataSetArray[1];

            Tuple<Regressor, SphericalVariogram> tuple = trainOrdinaryKriging(trainRegressionDataSet);
            Regressor regressor = tuple.first;
            SphericalVariogram sphericalVariogram = tuple.second;

            ErrorEntity testErrorEntity = new ErrorEntity(
                ErrorEntity.calError(testRegressionDataSet.getDPPList(), regressor)
            );

            ErrorEntity trainErrorEntity = new ErrorEntity(
                ErrorEntity.calError(trainRegressionDataSet.getDPPList(), regressor)
            );

            Method setSoilNutrientMethod =
                    LandEntity.class.getMethod("set" + soilNutrientEnum, NutrientEntity.class);

            InterpolationTask interpolationTask = new InterpolationTask(
                    regressor, originalPoint, sphericalVariogram, setSoilNutrientMethod, trainErrorEntity, testErrorEntity
            );
            interpolationTaskMap.put(soilNutrientEnum, interpolationTask);
        }

        for(LandEntity landEntity : landEntityList){
            interpolate(landEntity, interpolationTaskMap, cellSize, soilNutrientEnumArray);
        }

        return landEntityList;
    }

    private static LandEntity interpolate(
        LandEntity landEntity, Map<SoilNutrientEnum, InterpolationTask> interpolationTaskMap, double cellSize,
        final SoilNutrientEnum[] soilNutrientEnumArray
    ) throws InvocationTargetException, IllegalAccessException {
        final Geometry geometry = landEntity.getMultiPolygon();
        final Envelope envelope = geometry.getEnvelopeInternal();
        final double halfCellSize = cellSize / 2;

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
                        nutrientArray[i] += interpolationTask.getRegressor().regress(
                            GeoUtil.buildDataPoint(point)
                        );
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

    public static Tuple<Regressor, SphericalVariogram> trainOrdinaryKriging(RegressionDataSet trainRegressionDataSet){
        return trainOrdinaryKriging(trainRegressionDataSet, 100, 35000);
    }

    public static Tuple<Regressor, SphericalVariogram> trainOrdinaryKriging(
        RegressionDataSet trainRegressionDataSet, double lag, double lagDistance
    ){
        SphericalVariogram sphericalVariogram = new SphericalVariogram();
        SemiCloud<SphericalVariogram> semiCloud = new SemiCloud<>(
            trainRegressionDataSet, lag, lagDistance, sphericalVariogram
        );
        sphericalVariogram = semiCloud.trainVariogram();

        System.out.println(sphericalVariogram);
        System.out.println("RMSE: " + semiCloud.loss(sphericalVariogram));

        FixOrdinaryKriging ordinaryKriging = new FixOrdinaryKriging(sphericalVariogram, 0.0, sphericalVariogram.getNugget());
        ordinaryKriging.train(trainRegressionDataSet);
        return new Tuple<>(ordinaryKriging, sphericalVariogram);
    }
}
