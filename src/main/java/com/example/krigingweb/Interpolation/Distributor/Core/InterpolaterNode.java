package com.example.krigingweb.Interpolation.Distributor.Core;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InterpolaterNode {
    public final UUID id;
    public final int maxTaskNumber;
    private final AtomicInteger restTaskNumber;
    public final String url;

    /* 剩余容许异常次数，连续三次异常则该结点瘫痪 */
    private final AtomicInteger restExceptionNumber = new AtomicInteger(3);
    public InterpolaterNode(UUID id, int maxTaskNumber, String url) {
        this.id = id;
        this.maxTaskNumber = maxTaskNumber;
        this.restTaskNumber = new AtomicInteger(maxTaskNumber);
        this.url = url;
    }

    public void exception(){
        this.restExceptionNumber.decrementAndGet();
    }

    public void working(){
        this.restTaskNumber.incrementAndGet();
    }

    public boolean couldWork(){
        return this.restExceptionNumber.get() >= 0;
    }

    public void addTask(){
        this.restTaskNumber.getAndDecrement();
    }

    public int getRestTaskNumber(){
        return this.restTaskNumber.get();
    }
}
