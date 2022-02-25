package com.example.krigingweb.Interpolation.Distributor;

import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class InterpolaterStore {
    /* 用于存储所有的插值线程的UUID */
    private final Queue<UUID> interpolaterQueue = new ConcurrentLinkedQueue<>();
    private final Map<UUID, String> interpolaterURLMap = new HashMap<>();

    /* 单个interpolater未完成插值任务的限制数量 */
    @Value(value = "${distributor.undoneTaskLimitPerInterpolater}")
    private int undoneTaskLimitPerInterpolater = 100;
    private final AtomicInteger totalUndoneTaskLimit = new AtomicInteger(0);

    /* 限制当前最大未完成插值任务数量，避免冲垮插值集群 */
    private final Lock totalUndoneTaskLimitLock = new ReentrantLock();
    private final Condition totalUndoneTaskLimitCondition = this.totalUndoneTaskLimitLock.newCondition();

    private final UndoneTaskManager undoneTaskManager;

    public InterpolaterStore(UndoneTaskManager undoneTaskManager) {
        this.undoneTaskManager = undoneTaskManager;
    }

    public int registerInterpolater(UUID interpolaterID, String url){
        this.interpolaterQueue.add(interpolaterID);
        this.interpolaterURLMap.put(interpolaterID, url);
        this.totalUndoneTaskLimit.addAndGet(this.undoneTaskLimitPerInterpolater);
        return this.count();
    }

    public int deleteInterpolater(UUID interpolaterID){
        if(this.interpolaterURLMap.remove(interpolaterID) != null){
            this.totalUndoneTaskLimit.addAndGet(-this.undoneTaskLimitPerInterpolater);
        }
        return this.count();
    }

    /**
     * 当超出限制任务数量，则阻塞当前线程
     */
    private void checkLimit(){
        this.totalUndoneTaskLimitLock.lock();
        try {
            if(this.undoneTaskManager.getCount() > this.totalUndoneTaskLimit.get()){
                this.totalUndoneTaskLimitCondition.wait();
            }else{
                this.totalUndoneTaskLimitCondition.signalAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.totalUndoneTaskLimitLock.unlock();
        }
    }

    public UUID getInterpolater(){
        /* 总负载限制 */
        this.checkLimit();

        /* 负载均衡，采用循环队列分配任务 */
        UUID interpolaterID = this.interpolaterQueue.poll();
        if(interpolaterID != null){
            /* 判断interpolaterID是否还在 */
            if(this.interpolaterURLMap.get(interpolaterID) != null){
                /* 插回队尾 */
                this.interpolaterQueue.add(interpolaterID);
            }else{
                this.deleteInterpolater(interpolaterID);
                interpolaterID = null;
            }
        }

        if(interpolaterID == null && this.interpolaterURLMap.size() > 0){
            Thread.yield();
            interpolaterID = this.getInterpolater();
        }
        return interpolaterID;
    }

    public String getURL(UUID interpolaterID){
        return this.interpolaterURLMap.get(interpolaterID);
    }

    /**
     * @return 正在运行的插值结点
     */
    public int count(){
        return this.interpolaterURLMap.size();
    }
}
