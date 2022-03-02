package com.example.krigingweb.Interpolation.Core;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskData  {
    public UUID taskID;
    private ZonedDateTime createTime;
    private List<SamplePointEntity> samplePointEntityList;
    private List<LandEntity> landEntityList;

    public TaskData(List<SamplePointEntity> samplePointEntityList, List<LandEntity> landEntityList) {
        this(
            UUID.randomUUID(), ZonedDateTime.now(),
            samplePointEntityList, landEntityList
        );
    }

    public boolean isTimeOut(ZonedDateTime boundTime){
        return this.createTime.compareTo(boundTime) <= 0;
    }

    public void update(List<LandEntity> landEntityList){
        this.landEntityList = landEntityList;
        this.samplePointEntityList = null;
    }
}
