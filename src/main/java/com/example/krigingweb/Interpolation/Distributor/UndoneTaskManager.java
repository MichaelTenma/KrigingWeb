package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Core.ConcurrentMapQueue;
import com.example.krigingweb.Interpolation.Core.MapQueue;
import com.example.krigingweb.Interpolation.Core.TaskData;
import lombok.extern.slf4j.Slf4j;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
class UndoneTaskManager {
    /* 该队列用于超时控制 */
    private final MapQueue<UUID, TaskData> undoneTaskMapQueue = new ConcurrentMapQueue<>();

    @FunctionalInterface
    public interface TimeoutHandler {
        void handle(TaskData taskData);
    }

    public void addUndoneTask(TaskData taskData){
        this.undoneTaskMapQueue.add(taskData);
    }

    public TaskData doneTask(UUID taskID){
        return this.removeTask(taskID);
    }

    private TaskData removeTask(UUID taskID){
        return this.undoneTaskMapQueue.removeByKey(taskID);
    }

    public void timeout(long minutes, TimeoutHandler timeoutHandler){
        ZonedDateTime boundZonedDateTime = ZonedDateTime.now().minusMinutes(minutes);
        while(true){
            TaskData taskData = this.undoneTaskMapQueue.peek();
            if(taskData == null) break;

            if(taskData.isTimeOut(boundZonedDateTime) || !taskData.couldBeDistributed()){
                /* 该任务时间超时，移除该任务 */
                this.removeTask(taskData.taskID);
                taskData.invalid();
                timeoutHandler.handle(taskData);
                log.info("[UNDONE TASK TIMEOUT]: check timeout.");
            }else{
                /* 若队头任务未超时，则后续任务也不可能超时，直接退出循环 */
                break;
            }
        }
    }

    public int getCount(){
        return this.undoneTaskMapQueue.size();
    }
}


