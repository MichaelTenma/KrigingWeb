package com.example.krigingweb.Interpolation.Distributor.Core;

import com.example.krigingweb.Interpolation.Basic.HttpUtil;
import com.example.krigingweb.Interpolation.Core.MapQueueEntry;
import com.example.krigingweb.Interpolation.Core.TaskData;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InterpolaterNode implements MapQueueEntry<UUID> {
    public final UUID id;
    public final int maxTaskNumber;
    private final AtomicInteger restTaskNumber;
    public final String url;

    private ZonedDateTime lastHeartBeatTime = ZonedDateTime.now();

    public InterpolaterNode(UUID id, int maxTaskNumber, String url) {
        this.id = id;
        this.maxTaskNumber = maxTaskNumber;
        this.restTaskNumber = new AtomicInteger(maxTaskNumber);
        this.url = url;
    }

    public void incrementRestTaskNumber(){
        this.restTaskNumber.incrementAndGet();
    }
    public void decrementRestTaskNumber(){
        this.restTaskNumber.getAndDecrement();
    }

    public void addTask(TaskData taskData, RestTemplate restTemplate){
        HttpEntity<TaskData> httpEntity = new HttpEntity<>(taskData, HttpUtil.jsonHeaders);
        restTemplate.postForEntity(url, httpEntity, String.class);
    }

    public boolean isFullTask(){
        return this.restTaskNumber.get() <= 0;
    }

    public boolean isEmptyTask(){
        return this.restTaskNumber.get() >= this.maxTaskNumber;
    }

    public boolean couldAddTask(){
        return !this.isFullTask();
    }

    public boolean hasTask(){
        int num = this.restTaskNumber.get();
        return num >= 0 && num < this.restTaskNumber.get();
    }

    public void heartBeat(){
        this.lastHeartBeatTime = ZonedDateTime.now();
    }
    public boolean isValid(){
        return ZonedDateTime.now().minusMinutes(3).compareTo(this.lastHeartBeatTime) <= 0;
    }

    @Override
    public UUID mapQueueEntryKey() {
        return this.id;
    }
}
