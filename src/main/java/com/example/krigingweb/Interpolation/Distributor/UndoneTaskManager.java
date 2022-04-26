package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Core.TaskData;
import lombok.extern.slf4j.Slf4j;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class UndoneTaskManager {
    /* 该队列用于超时控制 */
    private final Queue<TaskData> undoneTaskQueue = new ConcurrentLinkedQueue<>();
    private final Map<UUID, TaskData> undoneTaskMap = new ConcurrentHashMap<>();
    private final AtomicInteger taskCount = new AtomicInteger(0);

    @FunctionalInterface
    public interface TimeoutHandler {
        void handle(TaskData taskData);
    }

    public void addUndoneTask(TaskData taskData){
        this.undoneTaskQueue.add(taskData);
        this.undoneTaskMap.put(taskData.taskID, taskData);
        this.taskCount.incrementAndGet();
    }

    public TaskData doneTask(UUID taskID){
        return this.removeTask(taskID);
    }

    private TaskData removeTask(UUID taskID){
        TaskData taskData = this.undoneTaskMap.remove(taskID);
        if(taskData != null){
            this.undoneTaskQueue.remove(taskData);
            this.taskCount.decrementAndGet();
        }
        return taskData;
    }

    public void timeout(long minutes, TimeoutHandler timeoutHandler){
        ZonedDateTime boundZonedDateTime = ZonedDateTime.now().minusMinutes(minutes);
        while(true){
            TaskData taskData = this.undoneTaskQueue.peek();
            if(taskData == null) break;

            if(taskData.isTimeOut(boundZonedDateTime) || !taskData.couldBeDistributed()){
                /* 该任务时间超时，移除该任务 */
                log.info("[UNDONE TASK TIMEOUT]: check timeout.");
                this.removeTask(taskData.taskID);
                taskData.invalid();
                timeoutHandler.handle(taskData);
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


