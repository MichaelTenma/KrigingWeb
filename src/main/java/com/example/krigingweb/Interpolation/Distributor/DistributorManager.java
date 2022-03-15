package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Distributor.Response.DoneTaskStatus;
import com.example.krigingweb.Service.LandService;
import com.example.krigingweb.Service.SamplePointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class DistributorManager {
    private final TaskGenerator taskGenerator;
    private final TaskStore taskStore;
    private final TaskDistributor taskDistributor;
    private final TaskUpdater taskUpdater;

    private final UndoneTaskManager undoneTaskManager;
    private final InterpolaterStore interpolaterStore;

    private final ExecutorService executorService;

    private final DistributorProperties distributorProperties;

    public DistributorManager(
            ExecutorService executorService, RestTemplate restTemplate,
            LandService landService, DistributorProperties distributorProperties,
            SamplePointService samplePointService
    ) {
        this.executorService = executorService;
        this.distributorProperties = distributorProperties;

        this.undoneTaskManager = new UndoneTaskManager(distributorProperties);
        this.interpolaterStore = new InterpolaterStore(this.undoneTaskManager, distributorProperties);

        this.taskUpdater = new TaskUpdater(landService);

        this.taskDistributor = new TaskDistributor(
            this.undoneTaskManager, this.interpolaterStore,
            restTemplate, executorService
        );
        this.taskStore = new TaskStore(this.taskDistributor);
        this.taskGenerator = new TaskGenerator(this.taskStore, landService, executorService, samplePointService);

        this.undoneTaskManager.setTimeoutHandler(this.taskStore::addTask);
        this.taskGenerator.setDoneHandler(() -> {
            if(landService.hasLandToInterpolate()){
                this.start();
//                this.stop();
            }else{
                /* 生成器任务完成，但不代表插值工作完成，还需要等待UndoneTask */
                log.info("[DISTRIBUTOR]: task generator done. ");
            }
        });

        this.taskDistributor.setInterpolaterExceptionHandler((interpolaterID, taskData) -> {
            /* 尝试重传十次仍然失败，则认为该结点已经瘫痪 */
            this.deleteInterpolater(interpolaterID);
            this.taskStore.addTask(taskData);
        });
    }

    /**
     * 完成该插值任务，并向数据库更新
     * @param taskID 插值任务ID
     * @param landEntityList 插值后的地块结果
     * @return 整个插值任务花费的秒数
     */
    public DoneTaskStatus doneTask(UUID taskID, List<LandEntity> landEntityList){
        TaskData taskData = this.undoneTaskManager.getUndoneTask(taskID);
        /* 注意死锁 */
        this.undoneTaskManager.doneTask(taskID);
        this.executorService.execute(
            () -> {
                this.taskUpdater.update(landEntityList);
            }
        );
        return new DoneTaskStatus(taskData);
    }

    public void registerInterpolater(UUID interpolaterID, String url) {
        if(this.interpolaterStore.registerInterpolater(interpolaterID, url) > 0){
            this.taskGenerator.resume();
        }
    }

    public void deleteInterpolater(UUID interpolaterID) {
        if(this.interpolaterStore.deleteInterpolater(interpolaterID) <= 0){
            this.taskGenerator.pause();
        }
    }

    public int countInterpolater(){
        return this.interpolaterStore.count();
    }

    /**
     * 一经开始，便只有完成全部地块的插值才能停止
     */
    public void start(){
        if(this.distributorProperties.isEnable()){
            log.info("[DISTRIBUTOR]: start");
            CompletableFuture.runAsync(this.taskGenerator::start);
//            this.executorService.execute(this.taskGenerator::start);
        }
    }

    public Map<UUID, String> getInterpolaterURLMap(){
        return this.interpolaterStore.getInterpolaterURLMap();
    }
}
