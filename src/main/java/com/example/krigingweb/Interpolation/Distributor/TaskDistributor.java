package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Interpolation.Core.TaskData;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

class TaskDistributor {
    private final TaskUpdater taskUpdater;

    private final RestTemplate restTemplate;
    private final UndoneTaskManager undoneTaskManager;
    private final InterpolaterStore interpolaterStore;

    public TaskDistributor(
        UndoneTaskManager undoneTaskManager, InterpolaterStore interpolaterStore,
        TaskUpdater taskUpdater, RestTemplate restTemplate
    ) {
        this.undoneTaskManager = undoneTaskManager;
        this.interpolaterStore = interpolaterStore;

        this.taskUpdater = taskUpdater;
        this.restTemplate = restTemplate;
    }

    public void doneTask(UUID taskID, List<LandEntity> landEntityList){
        this.undoneTaskManager.doneTask(taskID);
        this.taskUpdater.update(landEntityList);
    }

    public void distribute(TaskData[] taskDataArray) {
        /* 负载均衡，采用循环队列分配任务 */
        /* 需要拿到插值线程的UUID才能开始插值 */
        UUID interpolaterID = this.interpolaterStore.getInterpolater();
        for(TaskData taskData : taskDataArray){
            this.distribute(taskData, interpolaterID);
        }
    }

    private void distribute(TaskData taskData, UUID interpolaterID) {
        String url = this.interpolaterStore.getURL(interpolaterID);
        /* 必须发起异步请求 */
        this.restTemplate.postForEntity(url, taskData, );
        this.undoneTaskManager.addUndoneTask(taskData);
    }
}
