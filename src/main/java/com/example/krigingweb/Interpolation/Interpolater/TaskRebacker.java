package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Basic.HttpUtil;
import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Distributor.Response.DoneTaskStatus;
import com.example.krigingweb.Request.DoneTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
class TaskRebacker {

    private final String distributorURL;
    private final UUID interpolaterID;
    private final RestTemplate restTemplate;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
        new CustomizableThreadFactory("interpolater-rebacker-")
    );

    TaskRebacker(
        String distributorURL, UUID interpolaterID, RestTemplate restTemplate
    ) {
        this.distributorURL = distributorURL;
        this.interpolaterID = interpolaterID;
        this.restTemplate = restTemplate;
    }

    public void reback(TaskData taskData){
        log.info("[INTERPOLATER TASK]: taskID: " + taskData.taskID + ", " + taskData.errorMapToString());
        String url = this.distributorURL + "/distributor/doneTask";
        DoneTaskRequest doneTaskRequest
                = new DoneTaskRequest(interpolaterID, taskData.taskID, taskData.getLands());
        HttpEntity<DoneTaskRequest> httpEntity
                = new HttpEntity<>(doneTaskRequest, HttpUtil.jsonHeaders);

        CompletableFuture.runAsync(() -> {
            this.restTemplate.postForEntity(url, httpEntity, DoneTaskStatus.class);
        }, this.executorService);
    }
}
