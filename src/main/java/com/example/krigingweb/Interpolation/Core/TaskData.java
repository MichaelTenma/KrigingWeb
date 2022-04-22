package com.example.krigingweb.Interpolation.Core;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.Core.Kriging.VariogramPredictor;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TaskData  implements MemoryFree{
    public UUID taskID;
    public UUID belongInterpolaterID;
    private ZonedDateTime createTime;
    private ZonedDateTime postTime;
    private List<SamplePointEntity> samplePointEntityList;
    private List<LandEntity> landEntityList;

    private Map<SoilNutrientEnum, ErrorInfo> errorMap;
    private VariogramPredictor variogramPredictor;

    public TaskData(
        UUID taskID, ZonedDateTime createTime,
        List<SamplePointEntity> samplePointEntityList,
        List<LandEntity> landEntityList,
        Map<SoilNutrientEnum, ErrorInfo> errorMap,
        VariogramPredictor variogramPredictor
    ) {
        this.taskID = taskID;
        this.createTime = createTime;
        this.samplePointEntityList = samplePointEntityList;
        this.landEntityList = landEntityList;
        this.errorMap = errorMap;
        this.variogramPredictor = variogramPredictor;
    }

    public TaskData(List<SamplePointEntity> samplePointEntityList, List<LandEntity> landEntityList) {
        this(
            UUID.randomUUID(), ZonedDateTime.now(),
            samplePointEntityList, landEntityList,
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

    public void update(List<LandEntity> landEntityList){
        this.landEntityList = landEntityList;
        this.samplePointEntityList = null;
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
    public void free() {
        this.samplePointEntityList = null;
        this.landEntityList = null;
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
