package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Interpolater.Exception.InterpolaterException;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

class TaskBuffer {
    private static final int gapNum = 10;
    private final Queue<TaskData> data;
    private final AtomicInteger count;

    private final TaskInterpolater taskInterpolater;
    private final ExecutorService executorService;

    private final InterpolaterException.Handler interpolateExceptionHandler;

    public TaskBuffer(
        TaskInterpolater taskInterpolater, ExecutorService executorService,
        InterpolaterException.Handler interpolateExceptionHandler
    ) {
        this.taskInterpolater = taskInterpolater;
        this.executorService = executorService;
        this.interpolateExceptionHandler = interpolateExceptionHandler;
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

    private void commit(final int num) {
        for(int i = 0; i < num;i++){
            TaskData taskData = this.data.poll();
            if(taskData != null){
                CompletableFuture<TaskData> completableFuture = new CompletableFuture<>();
                this.executorService.submit(() -> {
                    try {
                        /* 分派任务到处理结点 */
                        this.taskInterpolater.interpolate(taskData);
                        completableFuture.complete(taskData);
                    } catch (InterpolaterException e) {
                        /* 忽视异常即可，当分派结点发现任务超时未完成时便会重发任务，
                         * 当然，会导致性能损耗，同时在插值结点出现问题时也无法及时发现
                         */
                        this.interpolateExceptionHandler.handle(taskData);
                        completableFuture.completeExceptionally(e);
                    }
                });
            }
        }
    }

    public void commitRest(){
        synchronized(this.data){
            this.count.set(0);
            this.commit(this.data.size());
        }
    }
}
