package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Core.TaskData;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

class TaskRebacker {

    private final UUID interpolaterID;
    private final RestTemplate restTemplate;

    TaskRebacker(UUID interpolaterID, RestTemplate restTemplate) {
        this.interpolaterID = interpolaterID;
        this.restTemplate = restTemplate;
    }

    public void reback(TaskData taskData){

    }
}
