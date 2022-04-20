package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Service.LandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
class TaskUpdater {
    private final LandService landService;
    private final ExecutorService updateExecutorService;

    public TaskUpdater(int maxUpdaterNumber, LandService landService) {
        this.landService = landService;
        this.updateExecutorService = new ThreadPoolExecutor(
            maxUpdaterNumber, maxUpdaterNumber, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new CustomizableThreadFactory("distributor-taskUpdater-")
        );
    }

    public CompletableFuture<Boolean> update(List<LandEntity> landEntityList){
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        this.updateExecutorService.execute(() -> {
            boolean isSuccess = true;
            for(int i = 0; i < 5;i++){
                try {
                    this.landService.updateLand(landEntityList);
                    isSuccess = true;
                    break;/* 正常情况执行一次即退出 */
                } catch (Exception e) {
                    log.warn("[DISTRIBUTOR]: 更新地块时发生异常！", e);
                    isSuccess = false;
                    Thread.yield();
                }
            }
            if(isSuccess){
                completableFuture.complete(true);
            }else{
                completableFuture.completeExceptionally(new Throwable());
            }
        });
        return completableFuture;
    }
}