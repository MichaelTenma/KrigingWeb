package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Basic.InterpolaterUtil;
import com.example.krigingweb.Interpolation.Interpolater.Exception.MaxInvalidNutrientValueNumException;
import com.example.krigingweb.Interpolation.Interpolater.Exception.TaskDataInterpolateException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

class TaskInterpolater {

    private final double cellSize;
    private final int maxInvalidNum;

    private final ExecutorService executorService;
    private final TaskDataInterpolateException.Handler taskDataInterpolateExceptionHandler;
    private final MaxInvalidNutrientValueNumException.Handler maxInvalidNutrientValueNumExceptionHandler;
    private final TaskRebacker taskRebacker;

    public TaskInterpolater(
            ExecutorService executorService, TaskRebacker taskRebacker, double cellSize, int maxInvalidNum,
            TaskDataInterpolateException.Handler taskDataInterpolateExceptionHandler,
            MaxInvalidNutrientValueNumException.Handler maxInvalidNutrientValueNumExceptionHandler
    ) {
        this.cellSize = cellSize;
        this.maxInvalidNum = maxInvalidNum;
        this.executorService = executorService;
        this.taskDataInterpolateExceptionHandler = taskDataInterpolateExceptionHandler;
        this.maxInvalidNutrientValueNumExceptionHandler = maxInvalidNutrientValueNumExceptionHandler;
        this.taskRebacker = taskRebacker;
    }


    public void addTask(TaskData taskData){
        if(taskData != null){
            CompletableFuture<TaskData> completableFuture = new CompletableFuture<>();
            this.executorService.submit(() -> {
                try {
                    /* 分派任务到处理结点 */
                    this.interpolate(taskData);
                    completableFuture.complete(taskData);
                } catch (TaskDataInterpolateException e) {
                    this.taskDataInterpolateExceptionHandler.handle(taskData);
                    completableFuture.completeExceptionally(e);
                } catch (MaxInvalidNutrientValueNumException e){
                    this.maxInvalidNutrientValueNumExceptionHandler.handle();
                    completableFuture.completeExceptionally(e);
                }
            });
        }
    }


    private void interpolate(
            TaskData taskData
    ) throws TaskDataInterpolateException, MaxInvalidNutrientValueNumException {
        try {
            List<LandEntity> landEntityList = InterpolaterUtil.interpolate(
                taskData, this.cellSize, this.maxInvalidNum, 200
            );

            landEntityList.forEach(landEntity -> landEntity.setMultiPolygon(null));
            taskData.update(landEntityList);
            this.taskRebacker.reback(taskData);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new TaskDataInterpolateException(taskData);
        }
    }
}
