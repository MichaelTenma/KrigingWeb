package com.example.krigingweb.Interpolation.Core;

public interface MapQueue<K, V extends MapQueueEntry<K>> {
    boolean add(V value);
    V removeByKey(K key);
    V removeByValue(V value);
    V poll();
    V peek();
    int size();
    boolean isEmpty();
    boolean containsKey(K key);
    boolean containsValue(V value);
}

