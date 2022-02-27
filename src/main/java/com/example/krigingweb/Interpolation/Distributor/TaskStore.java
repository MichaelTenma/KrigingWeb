package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Core.TaskData;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

class TaskStore {
    private static final int gapNum = 10;
    private final Queue<TaskData> data;
    private final AtomicInteger count;

    private final TaskDistributor taskDistributor;

    public TaskStore(TaskDistributor taskDistributor) {
        this.taskDistributor = taskDistributor;
        this.data = new ConcurrentLinkedQueue<>();
        this.count = new AtomicInteger(0);
    }

    public void addTask(TaskData taskData){
        this.data.add(taskData);
        if(this.count.addAndGet(1) == gapNum){
            this.count.set(0);
            this.commit(gapNum);
        }
    }

    private void commit(final int num){
        TaskData[] taskDataArray = new TaskData[num];
        for(int i = 0; i < num;i++){
            taskDataArray[i] = this.data.poll();
        }
        /* 分派任务到处理结点 */
        this.taskDistributor.distribute(taskDataArray);
    }

    public void commitRest(){
        synchronized(this.data){
            this.count.set(0);
            this.commit(this.data.size());
        }
    }
}
