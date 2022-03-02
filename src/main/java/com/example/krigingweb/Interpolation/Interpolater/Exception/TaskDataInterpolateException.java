package com.example.krigingweb.Interpolation.Interpolater.Exception;

import com.example.krigingweb.Interpolation.Core.TaskData;

public class TaskDataInterpolateException extends Exception{
    public TaskDataInterpolateException(TaskData taskData){
        super("插值任务：" + taskData.getTaskID() + " 发生异常！该任务的创建时间：" + taskData.getCreateTime());
    }

    @FunctionalInterface
    public interface Handler{
        void handle(TaskData taskData);
    }
}
