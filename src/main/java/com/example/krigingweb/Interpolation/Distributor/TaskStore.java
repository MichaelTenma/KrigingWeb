package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Core.TaskData;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskStore {
    private final Queue<TaskData> data;
    private final AtomicInteger count;

    private final int maxCount;
    private final ReentrantLock restLock = new ReentrantLock();
    private final Condition restLockCondition = restLock.newCondition();

    public TaskStore(int maxCount) {
        this.data = new ConcurrentLinkedQueue<>();
        this.count = new AtomicInteger(0);
        this.maxCount = maxCount;
    }

    public void addTask(TaskData taskData){
        if(this.count.getAndIncrement() >= this.maxCount){
            /* block */
            restLock.lock();
            restLockCondition.awaitUninterruptibly();
            restLock.unlock();
        }
        this.data.add(taskData);
    }

    public void retryTask(TaskData taskData){
        this.count.getAndIncrement();
        this.data.add(taskData);
    }

    public TaskData requestTask(){
        TaskData taskData = this.data.poll();
        if(taskData == null) return null;
        if(this.count.decrementAndGet() > 0){
            /* signal */
            restLock.lock();
            restLockCondition.signal();
            restLock.unlock();
        }
        return taskData;
    }

    public boolean isEmpty(){
        return this.count.get() <= 0;
    }
}
