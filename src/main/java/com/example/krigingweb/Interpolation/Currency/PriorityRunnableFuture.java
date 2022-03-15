package com.example.krigingweb.Interpolation.Currency;

import java.util.Comparator;
import java.util.concurrent.*;

class PriorityRunnableFuture<T> implements RunnableFuture<T> {
    private final RunnableFuture<T> src;
    private final PriorityEnum priorityEnum;

    public PriorityRunnableFuture(
        RunnableFuture<T> other, PriorityEnum priorityEnum
    ) {
        this.src = other;
        this.priorityEnum = priorityEnum;
    }

    public PriorityEnum getPriorityEnum() {
        return priorityEnum;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return src.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return src.isCancelled();
    }

    public boolean isDone() {
        return src.isDone();
    }

    public T get() throws InterruptedException, ExecutionException {
        return src.get();
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return src.get();
    }

    public void run() {
        src.run();
    }

    public static class PriorityFutureComparator implements Comparator<Runnable> {
        public int compare(Runnable o1, Runnable o2) {
            if (o1 == null && o2 == null)
                return 0;
            else if (o1 == null)
                return -1;
            else if (o2 == null)
                return 1;
            else {
                PriorityEnum p1 = PriorityEnum.weak;
                if(o1 instanceof PriorityRunnableFuture<?>){
                    p1 = ((PriorityRunnableFuture<?>) o1).getPriorityEnum();
                }

                PriorityEnum p2 = PriorityEnum.weak;
                if(o2 instanceof PriorityRunnableFuture<?>){
                    p2 = ((PriorityRunnableFuture<?>) o2).getPriorityEnum();
                }

                return Integer.compare(p1.getPriority(), p2.getPriority());
            }
        }
    }
}
