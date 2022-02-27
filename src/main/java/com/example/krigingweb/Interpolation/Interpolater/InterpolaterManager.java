package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Core.Enum.CallbackHttpEnum;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Interpolater.Exception.InterpolaterException;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class InterpolaterManager {
    private final UUID interpolaterID = UUID.randomUUID();

    private final TaskBuffer taskBuffer;
    private final TaskInterpolater taskInterpolater;
    private final TaskRebacker taskRebacker;

    private final RestTemplate restTemplate;

    private final AtomicInteger limitExceptionCount = new AtomicInteger(20);


    @Value(value = "${interpolater.distributorURL}")
    private String distributorURL;

    @Value(value = "${interpolater.callbackHttpEnum}")
    private CallbackHttpEnum callbackHttpEnum;

    public InterpolaterManager(ExecutorService executorService, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.taskRebacker = new TaskRebacker(interpolaterID, this.restTemplate);
        this.taskInterpolater = new TaskInterpolater(this.taskRebacker);

        InterpolaterException.Handler interpolateExceptionHandler = (TaskData taskData) -> {
            if(this.limitExceptionCount.decrementAndGet() >= 0){
                this.addTask(taskData);
            }else{
                this.error();
            }
        };

        this.taskBuffer = new TaskBuffer(this.taskInterpolater, executorService, interpolateExceptionHandler);
        this.register();
    }

    /**
     * 向分派结点注册
     */
    private void register(){
        JSONObject params = new JSONObject();
        params.put("interpolaterID", this.interpolaterID);
        params.put("callbackHttpEnum", callbackHttpEnum);
        params.put("url", "/interpolater/addTask");
        this.restTemplate.postForEntity(
            this.distributorURL + "/distributor/register",
            params, String.class
        );
    }

    public void addTask(TaskData taskData){
        this.taskBuffer.addTask(taskData);
    }

    private void error(){
        /* 告知分派结点：”当前插值结点已瘫痪“ */
        this.restTemplate.postForEntity(
            this.distributorURL + "/distributor/interpolaterException",
            this.interpolaterID, String.class
        );

        /* 并写入日志 */
        log.error(
            String.format("[INTERPOLATER ERROR]: 插值结点：%s 瘫痪。", this.interpolaterID)
        );

        /* 瘫痪当前结点 */
        System.exit(-1);
    }
}
