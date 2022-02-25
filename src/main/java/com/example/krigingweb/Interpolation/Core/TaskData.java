package com.example.krigingweb.Interpolation.Core;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.SamplePointEntity;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class TaskData {
    public final UUID taskID;
    private final ZonedDateTime createTime;

    private List<SamplePointEntity> samplePointEntityList;
    private List<LandEntity> landEntityList;

    public TaskData(List<SamplePointEntity> samplePointEntityList, List<LandEntity> landEntityList) {
        this(
            UUID.randomUUID(), ZonedDateTime.now(),
            samplePointEntityList, landEntityList
        );
    }

    public TaskData(
        UUID taskID, ZonedDateTime createTime,
        List<SamplePointEntity> samplePointEntityList,
        List<LandEntity> landEntityList
    ) {
        this.taskID = taskID;
        this.createTime = createTime;
        this.samplePointEntityList = samplePointEntityList;
        this.landEntityList = landEntityList;
    }

    public boolean isTimeOut(ZonedDateTime boundTime){
        return this.createTime.compareTo(boundTime) <= 0;
    }

    public void update(List<LandEntity> landEntityList){
        this.landEntityList = landEntityList;
        this.samplePointEntityList = null;
    }
}
