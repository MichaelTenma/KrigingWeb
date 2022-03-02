package com.example.krigingweb.Interpolation.Interpolater.Service;

import com.example.krigingweb.Exception.EmptyException;
import com.example.krigingweb.Interpolation.Core.TaskData;

import java.util.UUID;

public interface InterpolaterService {
    void addTask(TaskData taskData) throws EmptyException;
    UUID showID();
}
