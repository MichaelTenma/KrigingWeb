package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Basic.HttpUtil;
import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.Core.ErrorEntity;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Core.Util.Tuple;
import com.example.krigingweb.Interpolation.Distributor.Response.DoneTaskStatus;
import com.example.krigingweb.Request.DoneTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class TaskRebacker {

    private final String distributorURL;
    private final UUID interpolaterID;
    private final RestTemplate restTemplate;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
        new CustomizableThreadFactory("interpolater-rebacker-")
    );

    private final AtomicInteger taskCount = new AtomicInteger(0);
    private final Map<SoilNutrientEnum, TaskData.ErrorInfo> errorMap;

    TaskRebacker(
        String distributorURL, UUID interpolaterID, RestTemplate restTemplate
    ) {
        this.distributorURL = distributorURL;
        this.interpolaterID = interpolaterID;
        this.restTemplate = restTemplate;

        errorMap = new HashMap<>();
        for(SoilNutrientEnum soilNutrientEnum : SoilNutrientEnum.values()){
            ErrorEntity trainErrorEntity = new ErrorEntity(0.0, 0.0);
            ErrorEntity testErrorEntity = new ErrorEntity(0.0, 0.0);
            TaskData.ErrorInfo errorInfo = new TaskData.ErrorInfo(
                trainErrorEntity, testErrorEntity
            );
            errorMap.putIfAbsent(soilNutrientEnum, errorInfo);
        }
    }

    public String sumError(){
        StringBuilder sb = new StringBuilder();
        sb.append("sumError: {");
        synchronized (errorMap){
            int count = taskCount.get();
            errorMap.forEach((k, v)->{
                sb.append(k).append(": {");
                {
                    Double MAE = v.getTrainError().getMAE();
                    Double RMSE = v.getTrainError().getRMSE();
                    MAE /= count;
                    RMSE /= count;
                    sb.append(
                            String.format("trainError: {MAE: %f, RMSE: %f},", MAE, RMSE)
                    );
                }
                {
                    Double MAE = v.getTestError().getMAE();
                    Double RMSE = v.getTestError().getRMSE();
                    MAE /= count;
                    RMSE /= count;
                    sb.append(
                            String.format("testError: {MAE: %f, RMSE: %f}", MAE, RMSE)
                    );
                }
                sb.append("},");
            });
        }
        sb.append("}\n");
//        System.out.println(sb.toString());
        return sb.toString();
    }

    public void reback(TaskData taskData){
        log.info("[INTERPOLATER TASK]: taskID: " + taskData.taskID + ", " + taskData.errorMapToString());
        String url = this.distributorURL + "/distributor/doneTask";
        DoneTaskRequest doneTaskRequest
                = new DoneTaskRequest(interpolaterID, taskData.taskID, taskData.getLands());
        HttpEntity<DoneTaskRequest> httpEntity
                = new HttpEntity<>(doneTaskRequest, HttpUtil.jsonHeaders);

        synchronized (errorMap){
            taskCount.incrementAndGet();
            taskData.getErrorMap().forEach((k, v)->{
                TaskData.ErrorInfo errorInfo = errorMap.get(k);
                {
                    Double MAE = errorInfo.getTrainError().getMAE();
                    Double RMSE = errorInfo.getTrainError().getRMSE();
                    MAE += v.getTrainError().getMAE();
                    RMSE += v.getTrainError().getRMSE();
                    errorInfo.getTrainError().setMAE(MAE);
                    errorInfo.getTrainError().setRMSE(RMSE);
                }
                {
                    Double MAE = errorInfo.getTestError().getMAE();
                    Double RMSE = errorInfo.getTestError().getRMSE();
                    MAE += v.getTestError().getMAE();
                    RMSE += v.getTestError().getRMSE();
                    errorInfo.getTestError().setMAE(MAE);
                    errorInfo.getTestError().setRMSE(RMSE);
                }
            });
        }
        CompletableFuture.runAsync(() -> {
            this.restTemplate.postForEntity(url, httpEntity, DoneTaskStatus.class);
        }, this.executorService);
    }
}
