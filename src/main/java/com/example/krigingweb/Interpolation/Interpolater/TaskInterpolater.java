package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Core.Util.InterpolaterUtil;
import com.example.krigingweb.Interpolation.Interpolater.Exception.InterpolaterException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

class TaskInterpolater {

    private final double cellSize = 300;
    private final TaskRebacker taskRebacker;

    public TaskInterpolater(TaskRebacker taskRebacker) {
        this.taskRebacker = taskRebacker;
    }

    public void interpolate(TaskData taskData) throws InterpolaterException {
        try {
            List<LandEntity> landEntityList = InterpolaterUtil.interpolate(
                taskData.getSamplePointEntityList(),
                taskData.getLandEntityList(),
                this.cellSize
            );
            taskData.update(landEntityList);
            this.taskRebacker.reback(taskData);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new InterpolaterException(interpolaterID);
        }
    }
}
