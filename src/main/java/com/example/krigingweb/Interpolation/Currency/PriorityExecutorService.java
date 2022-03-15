package com.example.krigingweb.Interpolation.Currency;

import java.util.concurrent.*;

public class PriorityExecutorService extends ThreadPoolExecutor {

    public PriorityExecutorService(
        int corePoolSize, int maximumPoolSize, long keepAliveTime,
        TimeUnit unit, int initialCapacity, ThreadFactory threadFactory
    ) {
        super(
            corePoolSize, maximumPoolSize, keepAliveTime, unit,
            new PriorityBlockingQueue<>(
                initialCapacity, new PriorityRunnableFuture.PriorityFutureComparator()
            ), threadFactory
        );
    }

    protected <T> RunnableFuture<T> newTaskFor(
        Runnable runnable, T value, PriorityEnum priorityEnum
    ) {
        return new PriorityRunnableFuture<>(
            super.newTaskFor(runnable, value), priorityEnum
        );
    }

//    protected <T> RunnableFuture<T> newTaskFor(
//        Callable<T> callable, PriorityEnum priorityEnum
//    ) {
//        return new PriorityRunnableFuture<>(
//            super.newTaskFor(callable), priorityEnum
//        );
//    }

    public void execute(Runnable command, PriorityEnum priorityEnum) {
        super.execute(this.newTaskFor(command, null,priorityEnum));
    }
//
//    public Future<?> submit(Runnable task, PriorityEnum priorityEnum) {
//        if (task == null) throw new NullPointerException();
//        RunnableFuture<Void> ftask = newTaskFor(task, null, priorityEnum);
//        execute(ftask);
//        return ftask;
//    }
//
//    public <T> Future<T> submit(Runnable task, T result, PriorityEnum priorityEnum) {
//        if (task == null) throw new NullPointerException();
//        RunnableFuture<T> ftask = newTaskFor(task, result, priorityEnum);
//        execute(ftask);
//        return ftask;
//    }
//
//    public <T> Future<T> submit(Callable<T> task, PriorityEnum priorityEnum) {
//        if (task == null) throw new NullPointerException();
//        RunnableFuture<T> ftask = newTaskFor(task, priorityEnum);
//        execute(ftask);
//        return ftask;
//    }
//
//    @Override
//    public Future<?> submit(Runnable task) {
//        return this.submit(task, PriorityEnum.normal);
//    }
//
//    @Override
//    public <T> Future<T> submit(Runnable task, T result) {
//        return this.submit(task, result, PriorityEnum.normal);
//    }
//
//    @Override
//    public <T> Future<T> submit(Callable<T> task) {
//        return this.submit(task, PriorityEnum.normal);
//    }

    @Override
    public void execute(Runnable command) {
        this.execute(command, PriorityEnum.normal);
    }

}
