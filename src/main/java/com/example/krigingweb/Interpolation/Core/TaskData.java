package com.example.krigingweb.Interpolation.Core;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.Core.Kriging.VariogramPredictor;
import com.example.krigingweb.Serializer.UUIDJsonConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskData implements MapQueueEntry<UUID>, Serializable {
    @JsonSerialize(using = UUIDJsonConverter.Serializer.class)
    @JsonDeserialize(using = UUIDJsonConverter.Deserializer.class)
    public UUID taskID;

    @JsonSerialize(using = UUIDJsonConverter.Serializer.class)
    @JsonDeserialize(using = UUIDJsonConverter.Deserializer.class)
    public UUID belongInterpolaterID;
    private ZonedDateTime createTime;
    private ZonedDateTime postTime;
    private List<SamplePointEntity> samplePoints;
    private List<LandEntity> lands;

    private Map<SoilNutrientEnum, ErrorInfo> errorMap;
    private VariogramPredictor variogramPredictor;
    private final AtomicInteger maxInvalidNumber = new AtomicInteger(3);

    public TaskData(
        UUID taskID, ZonedDateTime createTime,
        List<SamplePointEntity> samplePoints,
        List<LandEntity> lands,
        Map<SoilNutrientEnum, ErrorInfo> errorMap,
        VariogramPredictor variogramPredictor
    ) {
        this.taskID = taskID;
        this.createTime = createTime;
        this.samplePoints = samplePoints;
        this.lands = lands;
        this.errorMap = errorMap;
        this.variogramPredictor = variogramPredictor;

        this.samplePoints.forEach(samplePointEntity -> {
            samplePointEntity.setSMC(null);
            samplePointEntity.setDMC(null);
            samplePointEntity.setYMC(null);
            samplePointEntity.setXMC(null);
            samplePointEntity.setCMC(null);
            samplePointEntity.setDistance(null);
            samplePointEntity.setId(null);
            samplePointEntity.setTime(null);
        });
    }

    public TaskData(List<SamplePointEntity> samplePoints, List<LandEntity> lands) {
        this(
            UUID.randomUUID(), ZonedDateTime.now(),
                samplePoints, lands,
            null, null
        );
    }

    public void setError(SoilNutrientEnum soilNutrientEnum, ErrorInfo errorInfo){
        if(this.errorMap == null) this.errorMap = new HashMap<>();
        this.errorMap.putIfAbsent(soilNutrientEnum, errorInfo);
    }

    public ErrorInfo getError(SoilNutrientEnum soilNutrientEnum){
        return this.errorMap.get(soilNutrientEnum);
    }

    public boolean isTimeOut(ZonedDateTime boundTime){
        return this.postTime.compareTo(boundTime) <= 0;
    }
    public boolean couldBeDistributed(){ return this.maxInvalidNumber.get() > 0; }
    public void invalid(){this.maxInvalidNumber.decrementAndGet();}

    public void update(List<LandEntity> landEntityList){
        this.lands = landEntityList;
        this.samplePoints = null;
    }

    public void updatePostTime() {
        this.postTime = ZonedDateTime.now();
    }

    public void updateCreateTime() {
        this.createTime = ZonedDateTime.now();
    }

    public String errorMapToString(){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<SoilNutrientEnum, ErrorInfo> entry : this.errorMap.entrySet()){
            sb.append(entry.getKey() + ": " + entry.getValue()).append(",");
        }
        sb.setCharAt(sb.length() - 1, ' ');
        return sb.toString();
    }

    @Override
    public UUID mapQueueEntryKey() {
        return this.taskID;
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorInfo{
        private final ErrorEntity trainError;
        private final ErrorEntity testError;

        @Override
        public String toString(){
            return String.format("{trainError: %s, testError: %s}", trainError, testError);
        }
    }
}
