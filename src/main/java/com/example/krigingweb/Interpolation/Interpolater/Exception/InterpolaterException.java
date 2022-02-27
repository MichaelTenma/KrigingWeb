package com.example.krigingweb.Interpolation.Interpolater.Exception;

import com.example.krigingweb.Interpolation.Core.TaskData;

import java.util.UUID;

public class InterpolaterException extends Exception{
    public final UUID interpolaterID;
    public InterpolaterException(UUID interpolaterID){
        super("插值结点：" + interpolaterID + "故障！");
        this.interpolaterID = interpolaterID;
    }

    @FunctionalInterface
    public interface Handler{
        void handle(TaskData taskData);
    }
}
