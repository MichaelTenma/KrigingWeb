package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Distributor.Exception.InterpolaterException;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

class TaskDistributor {
    private final RestTemplate restTemplate;
    private final UndoneTaskManager undoneTaskManager;
    private final InterpolaterStore interpolaterStore;
    private final ExecutorService executorService;

    @Setter
    private InterpolaterException.Handler interpolaterExceptionHandler;

    public TaskDistributor(
        UndoneTaskManager undoneTaskManager, InterpolaterStore interpolaterStore,
        TaskUpdater taskUpdater, RestTemplate restTemplate, ExecutorService executorService
    ) {
        this.undoneTaskManager = undoneTaskManager;
        this.interpolaterStore = interpolaterStore;

        this.restTemplate = restTemplate;
        this.executorService = executorService;
    }

    public void distribute(TaskData[] taskDataArray) {
        /* 负载均衡，采用循环队列分配任务 */
        /* 需要拿到插值线程的UUID才能开始插值 */
        UUID interpolaterID = this.interpolaterStore.getInterpolater();
        String url = this.interpolaterStore.getURL(interpolaterID);
        for(TaskData taskData : taskDataArray){
            this.distribute(taskData, interpolaterID, url);
        }
    }

    private CompletableFuture<Boolean> distribute(
        TaskData taskData, UUID interpolaterID, String url
    ) {
        /* 必须发起异步请求 */
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        this.executorService.submit(() -> {
            boolean isSuccess = this.postTaskData(url, taskData, 10);
            if(isSuccess){
                completableFuture.complete(true);
            }else{
                /* 尝试重传十次仍然失败，则认为该结点已经瘫痪 */
                this.interpolaterExceptionHandler.handle(interpolaterID, taskData);
                completableFuture.completeExceptionally(new InterpolaterException(interpolaterID));
            }
        });
        return completableFuture;
    }

    private boolean postTaskData(String url, TaskData taskData, int lessTry){
        if(lessTry <= 0)
            return false;

        ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(url, taskData, String.class);
        if(responseEntity.getStatusCode().equals(HttpStatus.OK)){
            this.undoneTaskManager.addUndoneTask(taskData);
            return true;
        }else{
            return this.postTaskData(url, taskData, lessTry - 1);
        }
    }
}
