package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Core.TaskData;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class UndoneTaskManager {
    /* 该队列用于超时控制 */
    private final Queue<UUID> undoneTaskQueue = new ConcurrentLinkedQueue<>();
    private final Map<UUID, TaskData> undoneTaskMap = new ConcurrentHashMap<>();
    private final AtomicInteger taskCount = new AtomicInteger(0);

    @Setter
    private TimeoutHandler timeoutHandler;

    private final DistributorProperties distributorProperties;

    UndoneTaskManager(DistributorProperties distributorProperties) {
        this.distributorProperties = distributorProperties;
    }

    @FunctionalInterface
    public interface TimeoutHandler {
        void handle(TaskData taskData);
    }

    public int addUndoneTask(TaskData taskData){
        this.undoneTaskQueue.add(taskData.taskID);
        this.undoneTaskMap.put(taskData.taskID, taskData);
        return this.taskCount.incrementAndGet();
    }

    public TaskData doneTask(UUID taskID){
        return this.removeTask(taskID);
    }

    private TaskData removeTask(UUID taskID){
        this.undoneTaskQueue.remove(taskID);
        this.taskCount.decrementAndGet();
        return this.undoneTaskMap.remove(taskID);
    }

    @Scheduled(initialDelay = 10 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
    private void timeout(){
        log.info("[UNDONE TASK TIMEOUT]: check timeout.");
        ZonedDateTime boundZonedDateTime = ZonedDateTime.now().minusMinutes(
            this.distributorProperties.getTimeoutMinutes()
        );

        while(true){
            UUID taskID = this.undoneTaskQueue.poll();
            if(taskID == null) break;

            TaskData taskData = this.undoneTaskMap.get(taskID);
            if(taskData == null) continue;

            if(taskData.isTimeOut(boundZonedDateTime)){
                /* 该任务时间超时，移除该任务 */
                this.removeTask(taskID);
                this.timeoutHandler.handle(taskData);
            }else{
                /* 若队头任务未超时，则后续任务也不可能超时，直接退出循环 */
                break;
            }
        }
    }

    public int getCount(){
        return this.taskCount.get();
    }
}
