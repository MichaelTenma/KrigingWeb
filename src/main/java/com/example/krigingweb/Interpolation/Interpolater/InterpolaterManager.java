package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Basic.HttpUtil;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Interpolater.Exception.TaskDataInterpolateException;
import com.example.krigingweb.Request.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class InterpolaterManager {
    public final UUID interpolaterID = UUID.randomUUID();

    private final TaskInterpolater taskInterpolater;
    private final TaskRebacker taskRebacker;

    private final RestTemplate restTemplate;

    private final AtomicInteger limitExceptionCount = new AtomicInteger(5);

    private final InterpolaterProperties interpolaterProperties;

    private final ScheduledExecutorService daemonExecutorService = Executors.newScheduledThreadPool(
        1, new CustomizableThreadFactory("interpolater-daemon-")
    );

    public InterpolaterManager(
        RestTemplate restTemplate,InterpolaterProperties interpolaterProperties
    ) {
        this.interpolaterProperties = interpolaterProperties;

        this.restTemplate = restTemplate;
        this.taskRebacker = new TaskRebacker(
            interpolaterProperties.getDistributorURL(), interpolaterID, this.restTemplate
        );

        TaskDataInterpolateException.Handler taskDataInterpolateExceptionHandler = (TaskData taskData) -> {
            if(this.limitExceptionCount.decrementAndGet() >= 0){
                this.addTask(taskData);
            }else{
                /* 不主动瘫痪结点 */
//                this.error();
            }
        };

        this.taskInterpolater = new TaskInterpolater(
            this.taskRebacker, this.interpolaterProperties.getCellSize(),
            interpolaterProperties.getConcurrentNumber(), taskDataInterpolateExceptionHandler
        );
        this.register();
    }

    private void heartBeat(){
        this.daemonExecutorService.scheduleAtFixedRate(() -> {
            this.restTemplate.getForObject(
                this.interpolaterProperties.getDistributorURL() + "/distributor/heartBeat" +
                        "?interpolaterID=" + this.interpolaterID, String.class
            );
        }, 0, 60, TimeUnit.SECONDS);
    }

    /**
     * 向分派结点注册，请勿主动调用注册，该方法将在应用启动后十秒自动执行一次
     */
    private void register(){
        this.daemonExecutorService.schedule(() -> {
            RegisterRequest registerRequest = new RegisterRequest(
                this.interpolaterID, "/interpolater/addTask",
                this.interpolaterProperties.getPort(),
                this.interpolaterProperties.getCallbackHttpEnum(),
                this.interpolaterProperties.getConcurrentNumber()
            );
            HttpEntity<RegisterRequest> httpEntity = new HttpEntity<>(registerRequest, HttpUtil.jsonHeaders);

            ResponseEntity<String> res = this.restTemplate.postForEntity(
                this.interpolaterProperties.getDistributorURL() + "/distributor/register",
                httpEntity, String.class
            );
            if(res.getStatusCode() == HttpStatus.OK){
                /* 启用心跳检测 */
                this.heartBeat();
            }
        }, 5, TimeUnit.SECONDS);
    }

    public void addTask(TaskData taskData){
        this.taskInterpolater.addTask(taskData);
    }

    private void error(){
        /* 告知分派结点：”当前插值结点已瘫痪“ */
        this.restTemplate.getForObject(
            this.interpolaterProperties.getDistributorURL() + "/distributor/interpolaterException" +
            "?interpolaterID=" + this.interpolaterID, String.class
        );

        /* 并写入日志 */
        log.error(
            String.format("[INTERPOLATER ERROR]: 插值结点：%s 瘫痪。", this.interpolaterID)
        );

        /* 瘫痪当前结点 */
        System.exit(-1);
    }
}
