package com.example.krigingweb.Interpolation.Distributor.Exception;

import com.example.krigingweb.Interpolation.Core.TaskData;

import java.util.UUID;

public class InterpolaterException extends Exception{
    public InterpolaterException(UUID interpolaterID){
        super("插值结点：" + interpolaterID + "故障！");
    }

    @FunctionalInterface
    public interface Handler{
        void handle(UUID interpolaterID, TaskData taskData);
    }
}
