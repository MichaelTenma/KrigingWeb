package com.example.krigingweb.Interpolation.Core;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.NutrientFilter;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.Core.Util.GeoUtil;
import com.example.krigingweb.Interpolation.Core.Kriging.MathUtil;
import com.example.krigingweb.Interpolation.Core.Kriging.OrdinaryKriging;
import com.example.krigingweb.Interpolation.Core.Util.Triple;
import com.example.krigingweb.Interpolation.Core.Util.Tuple;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterpolaterUtil {
    public static CompletableFuture<List<LandEntity>> interpolate(
        TaskData taskData, double cellSize, double lag, ExecutorService executorService, int concurrentNumber
    ) {
        CompletableFuture<List<LandEntity>> resCompletableFuture = new CompletableFuture<>();

        SoilNutrientEnum[] soilNutrientEnumArray = SoilNutrientEnum.values();
        final int nutrientLength = soilNutrientEnumArray.length;

        InterpolationTask[] interpolationTaskArray = Arrays.stream(soilNutrientEnumArray)
            .map(soilNutrientEnum -> {
//                executorService.submit(() -> {
//                    try {
//                        completableFuture.complete(train(taskData, soilNutrientEnum, lag));
//                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//                        completableFuture.completeExceptionally(e);
//                    }
//                });
//                completableFuture.exceptionally(throwable -> {
//                    resCompletableFuture.completeExceptionally(throwable);
//                    return null;
//                });
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        return train(taskData, soilNutrientEnum, lag);
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, executorService).exceptionally(throwable -> {
                    resCompletableFuture.completeExceptionally(throwable);
                    return null;
                });
            }).collect(Collectors.toList())
            .stream()
                .map(CompletableFuture::join)
                .toArray(InterpolationTask[]::new);

        final int perNum = 1000;
        final double halfCellSize = cellSize / 2;
        CompletableFuture<?>[] splitCompletableFutureArray = splitTask(taskData, concurrentNumber).stream()
            .map(landEntityList -> {
                return CompletableFuture.runAsync(() -> {
                    final double[][] each_u_array = new double[perNum][2];
                    for(LandEntity landEntity : landEntityList){
                        try {
                            interpolate(
                                    landEntity, halfCellSize, cellSize, each_u_array,
                                    nutrientLength, interpolationTaskArray
                            );
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, executorService)
                .exceptionally(throwable -> {
                    resCompletableFuture.completeExceptionally(throwable);
                    return null;
                });
//                executorService.submit(() -> {
////                    boolean isSuccess = true;
//                    final double[][] each_u_array = new double[perNum][2];
//                    for(LandEntity landEntity : landEntityList){
//                        try {
//                            interpolate(
//                                landEntity, halfCellSize, cellSize, each_u_array,
//                                nutrientLength, interpolationTaskArray
//                            );
//                        } catch (InvocationTargetException | IllegalAccessException e) {
//                            throw new RuntimeException(e);
////                            isSuccess = false;
////                            break;
//                        }
//                    }
////                    if(isSuccess) completableFuture.complete(null);
//                });

//                completableFuture.exceptionally(throwable -> {
//                   resCompletableFuture.completeExceptionally(throwable);
//                   return null;
//                });
//                return completableFuture;
            }).toArray(CompletableFuture<?>[]::new);
//            .collect(Collectors.toList());
//                .forEach(CompletableFuture::join);

        CompletableFuture.allOf(splitCompletableFutureArray).thenRunAsync(() -> {
            resCompletableFuture.complete(taskData.getLandEntityList());
        });
        /* 释放内存 */
//        Arrays.fill(interpolationTaskMap, null);
        return resCompletableFuture;
    }

    private static List<List<LandEntity>> splitTask(TaskData taskData, int concurrentNumber){
        List<List<LandEntity>> mapLandEntityList;
        {
            List<LandEntity> landEntityList = taskData.getLandEntityList();
            concurrentNumber = Math.min(concurrentNumber, landEntityList.size());
            mapLandEntityList = new ArrayList<>(concurrentNumber);
            final int eachLandNumber = (int) Math.ceil(landEntityList.size() * 1.0 / concurrentNumber);
            for(int i = 0; i < concurrentNumber;i++){
                mapLandEntityList.add(new ArrayList<>(eachLandNumber));
            }

            int index = 0;
            int i = 0;
            for(LandEntity landEntity : landEntityList){
                List<LandEntity> tempList = mapLandEntityList.get(index);
                tempList.add(landEntity);
                i++;
                if(i >= eachLandNumber){
                    index++;
                    i -= eachLandNumber;
                }
            }
        }
        return mapLandEntityList;
    }

    private static void interpolate(
        LandEntity landEntity, final double halfCellSize, final double cellSize,
        final double[][] each_u_array, int nutrientLength,
        final InterpolationTask[] interpolationTaskArray
    ) throws InvocationTargetException, IllegalAccessException {
        final int perNum = each_u_array.length;
        final Geometry geometry = landEntity.getMultiPolygon();
        final Envelope envelope = geometry.getEnvelopeInternal();
        final double[] nutrientArray = new double[nutrientLength];

        int i = 0, sumNum = 0;
        for(double beginY = envelope.getMinY(); beginY < envelope.getMaxY(); beginY += cellSize){
            for(double beginX = envelope.getMinX(); beginX < envelope.getMaxX(); beginX += cellSize){
                Coordinate coordinate = new Coordinate(beginX + halfCellSize, beginY + halfCellSize);
                Point point = GeoUtil.geometryFactory.createPoint(coordinate);
                if(geometry.isWithinDistance(point, halfCellSize)){
                    /* 一个地块同时插值多个指标 */
                    each_u_array[i][0] = beginX + halfCellSize;
                    each_u_array[i][1] = beginY + halfCellSize;

                    i++;
                    sumNum++;
                    if(i >= perNum){
                        System.out.println("满了");
                        regionSum(nutrientArray, each_u_array, interpolationTaskArray);
                        i -= perNum;
                    }
                }
            }
        }

        if(sumNum == 0){
            each_u_array[i][0] = envelope.getMinX() + halfCellSize;
            each_u_array[i][1] = envelope.getMinY() + halfCellSize;
            sumNum++;
            i++;
        }
        if(i > 0){
            double[][] temp_each_u_array = Arrays.copyOfRange(each_u_array, 0, i);
            regionSum(nutrientArray, temp_each_u_array, interpolationTaskArray);
        }

        for(int j = 0;j < nutrientArray.length;j++){
            interpolationTaskArray[j].setSoilNutrientMethod.invoke(landEntity, nutrientArray[j] / sumNum);
        }
    }

    private static void regionSum(
        final double[] nutrientArray, final double[][] each_u_array,
        final InterpolationTask[] interpolationTaskArray
    ){
        for(int j = 0;j < nutrientArray.length;j++){
            InterpolationTask interpolationTask = interpolationTaskArray[j];
            double[] predict_Z = interpolationTask.regressor.predict(each_u_array);
            for (double v : predict_Z) {
                nutrientArray[j] += v;
            }
        }
    }

    private static InterpolationTask train(
        TaskData taskData, SoilNutrientEnum soilNutrientEnum, double lag
    ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getSoilNutrientMethod = SamplePointEntity.class.getMethod("get" + soilNutrientEnum);

        /* 应该对采样点各个指标进行过滤 */
//        System.out.println("\n指标名称： " + soilNutrientEnum.name);
        List<SamplePointEntity> samplePointEntityList = taskData.getSamplePointEntityList();
        List<SamplePointEntity> filterSamplePointEntityList = samplePointEntityList.stream()
                .filter(NutrientFilter.get(soilNutrientEnum)).collect(Collectors.toList());

        int n = filterSamplePointEntityList.size();
        int train_n = (int) (n * 0.9);
        int test_n = n - train_n;

        double[][] train_u = new double[train_n][2];
        double[][] test_u = new double[test_n][2];
        for (int k = 0; k < train_n; k++) {
            train_u[k][0] = filterSamplePointEntityList.get(k).getGeom().getX();
            train_u[k][1] = filterSamplePointEntityList.get(k).getGeom().getY();
        }
        for (int k = train_n; k < n; k++) {
            test_u[k - train_n][0] = filterSamplePointEntityList.get(k).getGeom().getX();
            test_u[k - train_n][1] = filterSamplePointEntityList.get(k).getGeom().getY();
        }

        double[] train_Z = new double[train_n];
        double[] test_Z = new double[test_n];
        for (int k = 0; k < train_n; k++) {
            SamplePointEntity samplePointEntity = filterSamplePointEntityList.get(k);
            train_Z[k] = (double) getSoilNutrientMethod.invoke(samplePointEntity);
        }

        for (int k = train_n; k < n; k++) {
            SamplePointEntity samplePointEntity = filterSamplePointEntityList.get(k);
            test_Z[k - train_n] = (double) getSoilNutrientMethod.invoke(samplePointEntity);
        }

        OrdinaryKriging ordinaryKriging = new OrdinaryKriging(lag, train_u, train_Z);
        double[] predict_test_Z = ordinaryKriging.predict(test_u);
        double[] predict_train_Z = ordinaryKriging.predict(train_u);
        ErrorEntity testErrorEntity = new ErrorEntity(
                MathUtil.MAE(predict_test_Z, test_Z), MathUtil.RMSE(predict_test_Z, test_Z)
        );
        ErrorEntity trainErrorEntity = new ErrorEntity(
                MathUtil.MAE(predict_train_Z, train_Z), MathUtil.RMSE(predict_train_Z, train_Z)
        );

        taskData.setError(soilNutrientEnum, new TaskData.ErrorInfo(trainErrorEntity, testErrorEntity));

        Method setSoilNutrientMethod =
                LandEntity.class.getMethod("set" + soilNutrientEnum, Double.class);

        return new InterpolationTask(ordinaryKriging, setSoilNutrientMethod, soilNutrientEnum);
    }
}
