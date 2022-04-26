package com.example.krigingweb.Interpolation.Distributor.Core;

import com.example.krigingweb.Interpolation.Basic.HttpUtil;
import com.example.krigingweb.Interpolation.Core.TaskData;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InterpolaterNode {
    public final UUID id;
    public final int maxTaskNumber;
    private final AtomicInteger restTaskNumber;
    public final String url;

    private ZonedDateTime lastHeartBeatTime = ZonedDateTime.now();

    /* 剩余容许异常次数，连续三次异常则该结点瘫痪 */
//    private final AtomicInteger restExceptionNumber = new AtomicInteger(20);
    public InterpolaterNode(UUID id, int maxTaskNumber, String url) {
        this.id = id;
        this.maxTaskNumber = maxTaskNumber;
        this.restTaskNumber = new AtomicInteger(maxTaskNumber);
        this.url = url;
    }
    public void working(){
        this.restTaskNumber.incrementAndGet();
    }

    public void addTask(TaskData taskData, RestTemplate restTemplate){
        this.restTaskNumber.getAndDecrement();
        HttpEntity<TaskData> httpEntity = new HttpEntity<>(taskData, HttpUtil.jsonHeaders);
        restTemplate.postForEntity(url, httpEntity, String.class);
    }

    public int getRestTaskNumber(){
        return this.restTaskNumber.get();
    }

    public void heartBeat(){
        this.lastHeartBeatTime = ZonedDateTime.now();
    }

    public boolean isValid(){
        return ZonedDateTime.now().minusMinutes(3).compareTo(this.lastHeartBeatTime) <= 0;
    }
}
