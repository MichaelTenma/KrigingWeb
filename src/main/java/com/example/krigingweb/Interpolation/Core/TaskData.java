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
@AllArgsConstructor
public class TaskData  {
    public UUID taskID;
    private ZonedDateTime createTime;
    private List<SamplePointEntity> samplePointEntityList;
    private List<LandEntity> landEntityList;

    private Map<SoilNutrientEnum, ErrorInfo> errorMap;
    private VariogramPredictor variogramPredictor;

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
        return this.createTime.compareTo(boundTime) <= 0;
    }

    public void update(List<LandEntity> landEntityList){
        this.landEntityList = landEntityList;
        this.samplePointEntityList = null;
    }

    public String errorMapToString(){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<SoilNutrientEnum, ErrorInfo> entry : this.errorMap.entrySet()){
            sb.append(entry.getKey() + ": " + entry.getValue()).append(",");
        }
        sb.setCharAt(sb.length() - 1, ' ');
        return sb.toString();
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
