package com.example.krigingweb.Interpolation.Distributor.TaskGenerator;

import com.example.krigingweb.Interpolation.Basic.Enum.StatusEnum;
import com.example.krigingweb.Interpolation.Basic.StatusManage;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Distributor.Core.Rectangle;
import com.example.krigingweb.Interpolation.Distributor.TaskStore;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class AbstractTaskGenerator implements StatusManage, Runnable {

    /**
     * bias是用于兼容gcj02与wgs1984以及国家2000大地坐标系之间的差别，
     * 向四周扩宽5公里，保证整个广东在这三种坐标系下都涵盖在大矩形框中
     * 注意不包括东沙群岛，目前测土配方施肥的插值区域仅为广东省陆地区域
     */
    private static final double bias = 5000;
    private static final Rectangle domainRectangle = new Rectangle(
        12207881.844549, 13045561.012768, 2939884.095192, 2301216.219071
    ).bufferFromBorder(bias);

    private static final int rowNum = 128;// N
    private static final int colNum = 64;// M

    /* 向上取整，避免由于双精度浮点运算带来的舍入误差而导致某些地块不被涵盖在矩形框内 */
    private static final double x_gap = Math.ceil(domainRectangle.getWidth() / colNum);
    private static final double y_gap = Math.ceil(domainRectangle.getHeight() / rowNum);

    /* 线程共享 */
    private static StatusEnum statusEnum = StatusEnum.Stop;
    private static final ReentrantLock statusOpLock = new ReentrantLock();

    /* 线程独有 */
    private static final ReentrantLock statusLock = new ReentrantLock();
    private static final Condition statusLockCondition = statusLock.newCondition();

    /* 线程共享 */
    private static int totalThreadNumber;
    private static final AtomicInteger currentThreadNumber = new AtomicInteger(0);

    private final TaskStore taskStore;
    protected AbstractTaskGenerator(TaskStore taskStore, int totalThreadNumber) {
        this.taskStore = taskStore;
        AbstractTaskGenerator.totalThreadNumber = totalThreadNumber;
    }

    private void search(final int beginRow, final int perRowNumber){
        int endRow = beginRow + perRowNumber;
        if(endRow >= rowNum) endRow = rowNum - 1;
        for(int row = beginRow; row <= endRow; row++){
            log.info(String.format("[row]: %d", row));
            for(int col = 0; col < colNum; col++){
                while(!statusEnum.equals(StatusEnum.Run)){
                    statusLock.lock();
                    statusLockCondition.awaitUninterruptibly();
                    statusLock.unlock();
                }

                Rectangle rectangle = createRectangle(row, col);
                this.search(rectangle);
            }
        }
    }

    protected abstract void search(Rectangle rectangle);

    protected void commit(TaskData taskData){
        if(taskData != null) this.taskStore.addTask(taskData);
    }

    @Override
    public void run(){
        int threadIndex = currentThreadNumber.getAndIncrement();
        int perRowNumber = (int)Math.ceil(rowNum * 1.0 / totalThreadNumber);
        this.search(perRowNumber * threadIndex, perRowNumber);
    }

    @Override
    public void doStart(){
        if(statusEnum != StatusEnum.Run){
            statusOpLock.lock();
            statusEnum = StatusEnum.Run;
            statusOpLock.unlock();

            statusLock.lock();
            statusLockCondition.signalAll();
            statusLock.unlock();
        }
    }

    @Override
    public void doPause(){
        statusOpLock.lock();
        statusEnum = StatusEnum.Pause;
        statusOpLock.unlock();
    }

    @Override
    public void doResume() {
        this.doStart();
    }

    @Override
    public void doStop(){
        statusOpLock.lock();
        statusEnum = StatusEnum.Stop;
        statusOpLock.unlock();
    }

    /**
     * 将广东省划分成 N*M 份，注意从左下角开始
     * @param row 行，从0开始，[0, N)
     * @param col 列，从0开始，[0, M)
     * @return 矩形的WKT字符串
     */
    private static Rectangle createRectangle(int row, int col){
        if(
            row >= rowNum || row < 0 ||
            col >= colNum || col < 0
        ) return null;

        final double left = domainRectangle.left + col * x_gap;
        final double right = left + x_gap;
        final double bottom = domainRectangle.bottom + row * y_gap;
        final double top = bottom + y_gap;
        return new Rectangle(left, right, top, bottom);
    }

    public static Rectangle getExtent(){
        return domainRectangle;
    }
}
