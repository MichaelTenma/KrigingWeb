package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Core.TaskData;

public class InterpolateException extends Exception{
    public InterpolateException(){
        super("插值时发生异常！");
    }

    @FunctionalInterface
    public interface Handler{
        void handle(TaskData taskData);
    }
}
