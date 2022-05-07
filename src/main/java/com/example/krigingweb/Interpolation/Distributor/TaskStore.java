package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Core.TaskData;

import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskStore {
    private final Queue<TaskData> data;
    private final AtomicInteger count;

    private final Queue<TaskData> exceptionQueue;

    private final int maxCount;
    private final ReentrantLock restLock = new ReentrantLock();
    private final Condition restLockCondition = restLock.newCondition();

    public TaskStore(int maxCount) {
        this.data = new ConcurrentLinkedQueue<>();
        this.count = new AtomicInteger(0);
        this.maxCount = maxCount;

        this.exceptionQueue = new ConcurrentLinkedQueue<>();
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
        this.exceptionQueue.add(taskData);
    }

    public TaskData requestTask(){
        /* 先从exceptionQueue中取 */
        TaskData taskData = this.exceptionQueue.poll();
        if(taskData == null){
            taskData = this.data.poll();

            if(taskData == null) return null;
            if(this.count.getAndDecrement() > this.maxCount){
                /* signal */
                restLock.lock();
                restLockCondition.signal();
                restLock.unlock();
            }
        }else{
//            System.out.println("取自exceptionQueue");
        }
        return taskData;
    }

    public boolean isEmpty(){
        return this.count.get() <= 0;
    }
}
