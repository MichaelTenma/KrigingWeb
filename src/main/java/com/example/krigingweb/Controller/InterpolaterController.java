package com.example.krigingweb.Controller;

import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Interpolater.InterpolaterManager;

public class InterpolaterController {

    private final InterpolaterManager interpolaterManager;

    public InterpolaterController(InterpolaterManager interpolaterManager) {
        this.interpolaterManager = interpolaterManager;
    }

    public void addTask(TaskData taskData){
        this.interpolaterManager.addTask(taskData);
    }
}
