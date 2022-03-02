package com.example.krigingweb.Interpolation.Interpolater.Service.Impl;

import com.example.krigingweb.Exception.EmptyException;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Interpolater.InterpolaterManager;
import com.example.krigingweb.Interpolation.Interpolater.Service.InterpolaterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class InterpolaterServiceImpl implements InterpolaterService {
    private final InterpolaterManager interpolaterManager;

    @Autowired
    public InterpolaterServiceImpl(InterpolaterManager interpolaterManager) {
        this.interpolaterManager = interpolaterManager;
    }

    @Override
    public void addTask(TaskData taskData) throws EmptyException {
        EmptyException.check("taskData", taskData);
        this.interpolaterManager.addTask(taskData);

        log.info("[INTERPOLATER]: add task " + taskData.taskID);
    }

    @Override
    public UUID showID(){
        return this.interpolaterManager.interpolaterID;
    }
}
