package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Basic.HttpUtil;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Distributor.Response.DoneTaskStatus;
import com.example.krigingweb.Request.DoneTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
class TaskRebacker {

    private final String distributorURL;
    private final UUID interpolaterID;
    private final RestTemplate restTemplate;

    TaskRebacker(String distributorURL, UUID interpolaterID, RestTemplate restTemplate) {
        this.distributorURL = distributorURL;
        this.interpolaterID = interpolaterID;
        this.restTemplate = restTemplate;
    }

    public void reback(TaskData taskData){
        log.info("[INTERPOLATER TASK]: taskID: " + taskData.taskID + ", " + taskData.errorMapToString());
        CompletableFuture.supplyAsync(() -> {
            String url = this.distributorURL + "/distributor/doneTask";
            DoneTaskRequest doneTaskRequest
                    = new DoneTaskRequest(interpolaterID, taskData.taskID, taskData.getLandEntityList());

            HttpEntity<DoneTaskRequest> httpEntity
                    = new HttpEntity<>(doneTaskRequest, HttpUtil.jsonHeaders);

            ResponseEntity<DoneTaskStatus> responseEntity
                    = this.restTemplate.postForEntity(url, httpEntity, DoneTaskStatus.class);

            if(responseEntity.getStatusCode().equals(HttpStatus.OK)){
                return responseEntity.getBody();
            }else{
                return null;
            }
        });
    }
}
