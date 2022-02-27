package com.example.krigingweb.Interpolation.Distributor.Response;

import com.example.krigingweb.Interpolation.Core.TaskData;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

public class DoneTaskStatus {
    public final UUID taskID;
    public final long seconds;

    public DoneTaskStatus(TaskData taskData){
        this.taskID = taskData.getTaskID();
        Instant currentInstant = ZonedDateTime.now().toInstant();
        Instant createInstant = taskData.getCreateTime().toInstant();
        this.seconds = currentInstant.getEpochSecond() - createInstant.getEpochSecond();
    }

    @Override
    public String toString(){
        return String.format("taskID: %s, seconds: %d", taskID, seconds);
    }
}
