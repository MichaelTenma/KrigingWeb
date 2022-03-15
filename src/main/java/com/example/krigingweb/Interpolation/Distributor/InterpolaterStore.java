package com.example.krigingweb.Interpolation.Distributor;

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

    private final AtomicInteger totalUndoneTaskLimit = new AtomicInteger(0);

    /* 限制当前最大未完成插值任务数量，避免冲垮插值集群 */
    private final Lock totalUndoneTaskLimitLock = new ReentrantLock();
    private final Condition totalUndoneTaskLimitCondition = this.totalUndoneTaskLimitLock.newCondition();

    private final Lock zeroInterpolaterLimitLock = new ReentrantLock();
    private final Condition zeroInterpolaterLimitCondition = this.zeroInterpolaterLimitLock.newCondition();

    private final UndoneTaskManager undoneTaskManager;

    private final DistributorProperties distributorProperties;

    public InterpolaterStore(UndoneTaskManager undoneTaskManager, DistributorProperties distributorProperties) {
        this.undoneTaskManager = undoneTaskManager;
        this.distributorProperties = distributorProperties;

        this.undoneTaskManager.addWhenDone(() -> {
            this.totalUndoneTaskLimitLock.lock();
            if(this.undoneTaskManager.getCount() <= this.totalUndoneTaskLimit.get()){
                this.totalUndoneTaskLimitCondition.signal();
            }
            this.totalUndoneTaskLimitLock.unlock();

        });
    }

    public int registerInterpolater(UUID interpolaterID, String url){
        this.interpolaterQueue.add(interpolaterID);
        this.interpolaterURLMap.put(interpolaterID, url);
        this.totalUndoneTaskLimit.addAndGet(this.distributorProperties.getUndoneTaskLimitPerInterpolater());

        {
            this.zeroInterpolaterLimitLock.lock();
            this.zeroInterpolaterLimitCondition.signalAll();
            this.zeroInterpolaterLimitLock.unlock();
        }

        return this.count();
    }

    public int deleteInterpolater(UUID interpolaterID){
        if(this.interpolaterURLMap.remove(interpolaterID) != null){
            this.totalUndoneTaskLimit.addAndGet(-this.distributorProperties.getUndoneTaskLimitPerInterpolater());
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
                this.totalUndoneTaskLimitCondition.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.totalUndoneTaskLimitLock.unlock();
        }
    }

    /**
     * 零插值结点阻塞
     */
    private void zeroInterpolaterLimit(){
        this.zeroInterpolaterLimitLock.lock();
        try {
            if(this.count() <= 0)
                this.zeroInterpolaterLimitCondition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.zeroInterpolaterLimitLock.unlock();
        }
    }

    /**
     * 随机获取一个插值结点，无需考虑返回值为null的情况
     * @return 插值结点的UUID
     */
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
        }else{
            /* 零插值结点阻塞 */
            this.zeroInterpolaterLimit();
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

    public Map<UUID, String> getInterpolaterURLMap(){
        return this.interpolaterURLMap;
    }
}
