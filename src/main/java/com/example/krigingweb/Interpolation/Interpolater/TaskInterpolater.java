package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Core.InterpolaterUtil;
import com.example.krigingweb.Interpolation.Interpolater.Exception.TaskDataInterpolateException;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.*;
import java.util.function.Function;

class TaskInterpolater {

    private final double cellSize;

    private final ExecutorService executorService;
    private final int concurrentNumber;
    private final TaskDataInterpolateException.Handler taskDataInterpolateExceptionHandler;
    private final TaskRebacker taskRebacker;

    public TaskInterpolater(
        TaskRebacker taskRebacker, double cellSize, int concurrentNumber,
        TaskDataInterpolateException.Handler taskDataInterpolateExceptionHandler
    ) {
        this.cellSize = cellSize;
        this.taskDataInterpolateExceptionHandler = taskDataInterpolateExceptionHandler;
        this.taskRebacker = taskRebacker;
        this.concurrentNumber = concurrentNumber;
        this.executorService = new ThreadPoolExecutor(
            concurrentNumber, concurrentNumber, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new CustomizableThreadFactory("interpolater-")
        );
    }

    public void addTask(TaskData taskData){
        if(taskData != null){
            /* 分派任务到处理结点 */
            /* lambda是一个匿名内部类，该匿名内部类被GCRoot引用，GCRoot -> lambda -> taskData无法释放故内存泄漏 */
            CompletableFuture.runAsync(() -> {
                this.interpolate(taskData).exceptionally(throwable -> {
//                    throwable.printStackTrace();
                    this.taskDataInterpolateExceptionHandler.handle(taskData);
                    return null;
                });
            });
//            this.executorService.submit(() -> {
//                this.interpolate(taskData).exceptionally(throwable -> {
//                    this.taskDataInterpolateExceptionHandler.handle(taskData);
//                    return null;
//                });
//            });
        }
    }

    private CompletableFuture<Void> interpolate(TaskData taskData) {
        CompletableFuture<Void> resCompletableFuture = new CompletableFuture<>();
        InterpolaterUtil.interpolate(taskData, this.cellSize, 200, this.executorService, this.concurrentNumber)
            .thenAccept(landEntityList -> {
                landEntityList.forEach(landEntity -> landEntity.setMultiPolygon(null));
                taskData.update(landEntityList);
                this.taskRebacker.reback(taskData);
                resCompletableFuture.complete(null);
            }).exceptionally(throwable -> {
                throwable.printStackTrace();
                resCompletableFuture.completeExceptionally(new TaskDataInterpolateException(taskData));
                return null;
            });
        return resCompletableFuture;
    }
}
