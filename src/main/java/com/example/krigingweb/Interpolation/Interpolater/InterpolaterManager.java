package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Basic.HttpUtil;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Interpolater.Exception.MaxInvalidNutrientValueNumException;
import com.example.krigingweb.Interpolation.Interpolater.Exception.TaskDataInterpolateException;
import com.example.krigingweb.Request.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class InterpolaterManager {
    public final UUID interpolaterID = UUID.randomUUID();

    private TaskInterpolater taskInterpolater;
    private TaskRebacker taskRebacker;

    private RestTemplate restTemplate;

    private AtomicInteger limitExceptionCount = new AtomicInteger(5);

    private final InterpolaterProperties interpolaterProperties;

    public InterpolaterManager(
        ExecutorService executorService, RestTemplate restTemplate,
        InterpolaterProperties interpolaterProperties
    ) {
        this.interpolaterProperties = interpolaterProperties;
        if(!this.interpolaterProperties.isEnable()) return;

        this.restTemplate = restTemplate;
        this.taskRebacker = new TaskRebacker(interpolaterProperties.getDistributorURL(), interpolaterID, this.restTemplate);

        TaskDataInterpolateException.Handler taskDataInterpolateExceptionHandler = (TaskData taskData) -> {
            if(this.limitExceptionCount.decrementAndGet() >= 0){
                this.addTask(taskData);
            }else{
                this.error();
            }
        };

        MaxInvalidNutrientValueNumException.Handler maxInvalidNutrientValueNumExceptionHandler = () -> {
            log.warn("[INTERPOLATER]: 插值结果中具有过多的无效值！");
        };
        this.taskInterpolater = new TaskInterpolater(
            executorService, this.taskRebacker,
            this.interpolaterProperties.getCellSize(),
            this.interpolaterProperties.getMaxInvalidNum(),
            taskDataInterpolateExceptionHandler,
            maxInvalidNutrientValueNumExceptionHandler
        );

//        this.taskBuffer = new TaskBuffer(this.taskInterpolater, executorService, taskDataInterpolateExceptionHandler, maxInvalidNutrientValueNumExceptionHandler);
        this.register();
    }

    /**
     * 向分派结点注册，请勿主动调用注册，该方法将在应用启动后十秒自动执行一次
     */
    private void register(){
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.yield();
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }

//            MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
            RegisterRequest registerRequest = new RegisterRequest(
                this.interpolaterID, "/interpolater/addTask",
                this.interpolaterProperties.getPort(), this.interpolaterProperties.getCallbackHttpEnum()
            );
//            JSONObject params = new JSONObject();
//            params.put("interpolaterID", this.interpolaterID);
//            params.put("callbackHttpEnum", this.interpolaterProperties.getCallbackHttpEnum());
//            params.put("apiPath", "/interpolater/addTask");
//            params.put("port", this.interpolaterProperties.getPort());
            HttpEntity httpEntity = new HttpEntity(registerRequest, HttpUtil.jsonHeaders);

            return this.restTemplate.postForEntity(
                this.interpolaterProperties.getDistributorURL() + "/distributor/register",
                httpEntity, String.class
            );
        });

    }

    public void addTask(TaskData taskData){
        this.taskInterpolater.addTask(taskData);
    }

    private void error(){
        /* 告知分派结点：”当前插值结点已瘫痪“ */
        this.restTemplate.postForEntity(
            this.interpolaterProperties.getDistributorURL() + "/distributor/interpolaterException",
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
