package com.example.krigingweb.Interpolation.Core;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentMapQueue<K, V extends MapQueueEntry<K>> implements MapQueue<K, V>{
    private final Queue<V> queue;
    private final Map<K, V> map;
    private final AtomicInteger count;

    public ConcurrentMapQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.map = new ConcurrentHashMap<>();
        this.count = new AtomicInteger(0);
    }

    @Override
    public boolean add(V value) {
        boolean isSuccess = false;
        K key = value.mapQueueEntryKey();
        if(!this.containsKey(key)){
            this.map.put(key, value);
            this.queue.add(value);
            this.count.incrementAndGet();
            isSuccess = true;
        }
        return isSuccess;
    }

    @Override
    public V removeByKey(K key) {
        V value = this.map.remove(key);
        if(value != null){
            this.queue.remove(value);
            this.count.decrementAndGet();
        }
        return value;
    }

    @Override
    public V removeByValue(V value) {
        return this.removeByKey(value.mapQueueEntryKey());
    }

    @Override
    public V poll() {
        V value = this.queue.poll();
        if(value != null){
            this.map.remove(value.mapQueueEntryKey());
            this.count.decrementAndGet();
        }
        return value;
    }

    @Override
    public V peek() {
        return this.queue.peek();
    }

    @Override
    public int size() {
        return this.count.get();
    }

    @Override
    public boolean isEmpty() {
        return this.count.get() == 0;
    }

    @Override
    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return this.containsKey(value.mapQueueEntryKey());
    }
}
