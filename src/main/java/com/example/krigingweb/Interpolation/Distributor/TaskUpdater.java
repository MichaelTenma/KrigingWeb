package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Service.LandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
class TaskUpdater {
    private final LandService landService;
    private final ExecutorService updateExecutorService;

    public TaskUpdater(int maxUpdaterNumber, LandService landService) {
        this.landService = landService;

        this.updateExecutorService = Executors.newFixedThreadPool(
            maxUpdaterNumber,
            new CustomizableThreadFactory("distributor-taskUpdater-")
        );
//        this.updateExecutorService = new ThreadPoolExecutor(
//            maxUpdaterNumber, maxUpdaterNumber, 0L, TimeUnit.MILLISECONDS,
//            new LinkedBlockingQueue<>(), new CustomizableThreadFactory("distributor-taskUpdater-")
//        );
    }

    public CompletableFuture<Void> update(List<LandEntity> landEntityList){
        return CompletableFuture.runAsync(() -> {
            int i = 0;
            for(; i < 5;i++){
                try {
                    this.landService.updateLand(landEntityList);
                    break;/* 正常情况执行一次即退出 */
                } catch (Throwable e) {
                    e.printStackTrace();
                    Thread.yield();
                }
            }
            if(i == 5){
                throw new RuntimeException();
            }
        });
    }
}