package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Core.TaskData;
import lombok.extern.slf4j.Slf4j;
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

    public InterpolaterManager(ExecutorService executorService, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.taskRebacker = new TaskRebacker(interpolaterID, this.restTemplate);
        this.taskInterpolater = new TaskInterpolater(this.taskRebacker);

        InterpolateException.Handler interpolateExceptionHandler = (TaskData taskData) -> {
            if(this.limitExceptionCount.decrementAndGet() >= 0){
                this.addTask(taskData);
            }else{
                this.error();
            }
        };

        this.taskBuffer = new TaskBuffer(this.taskInterpolater, executorService, interpolateExceptionHandler);
    }

    public void addTask(TaskData taskData){
        this.taskBuffer.addTask(taskData);
    }

    private void error(){
        /* 告知分派结点：”当前插值结点已瘫痪“ */
//        this.restTemplate.postForEntity();

        /* 并写入日志 */
        log.error(
            String.format("[INTERPOLATER ERROR]: 插值结点：%s 瘫痪。", this.interpolaterID)
        );

        /* 瘫痪当前结点 */
        System.exit(-1);
    }
}
