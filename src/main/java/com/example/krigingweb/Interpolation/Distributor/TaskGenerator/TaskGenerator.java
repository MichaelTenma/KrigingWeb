package com.example.krigingweb.Interpolation.Distributor.TaskGenerator;

import com.example.krigingweb.Interpolation.Basic.Enum.StatusEnum;
import com.example.krigingweb.Interpolation.Basic.StatusManage;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Distributor.Core.Rectangle;
import com.example.krigingweb.Interpolation.Distributor.TaskStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class TaskGenerator implements StatusManage {

    /**
     * bias是用于兼容gcj02与wgs1984以及国家2000大地坐标系之间的差别，
     * 向四周扩宽5公里，保证整个广东在这三种坐标系下都涵盖在大矩形框中
     * 注意不包括东沙群岛，目前测土配方施肥的插值区域仅为广东省陆地区域
     */
    private static final double bias = 5000;
    private static final Rectangle domainRectangle = new Rectangle(
        12207881.844549, 13045561.012768, 2939884.095192, 2301216.219071
    ).bufferFromBorder(bias);

    private static final int rowNum;// N
    private static final int colNum;// M

    /* 向上取整，避免由于双精度浮点运算带来的舍入误差而导致某些地块不被涵盖在矩形框内 */
    private static final double x_gap;
    private static final double y_gap;

    static {
        final double expectedArea = 400_000000;/* 400平方公里 */
        final double expectedWidth = Math.sqrt(expectedArea);
        final double expectedHeight = expectedWidth;

        x_gap = expectedWidth;
        y_gap = expectedHeight;

        colNum = (int)Math.ceil(domainRectangle.getWidth() / expectedWidth);
        rowNum = (int)Math.ceil(domainRectangle.getHeight() / expectedHeight);

        log.info(
            "[DISTRIBUTOR]: total row " + rowNum + ", col " + colNum +
            ", x_gap " + x_gap + ", y_gap " + y_gap + ". "
        );
    }

    private StatusEnum statusEnum = StatusEnum.Stop;
    private final ReentrantLock statusOpLock = new ReentrantLock();

    private final ReentrantLock statusLock = new ReentrantLock();
    private final Condition statusLockCondition = statusLock.newCondition();

    private final int totalThreadNumber;
    private final ExecutorService executorService;

    private final TaskStore taskStore;

    private int current_row = 0;
    private int current_col = 0;
    private final ReentrantLock requestLock = new ReentrantLock();/* 锁住last_row与last_col的读写操作，mutex */
    private final static int each_num = 10;

    protected TaskGenerator(TaskStore taskStore, int totalThreadNumber) {
        this.taskStore = taskStore;
        this.totalThreadNumber = totalThreadNumber;

        this.executorService = Executors.newFixedThreadPool(
            totalThreadNumber, new CustomizableThreadFactory("distributor-taskGenerator-")
        );
    }


    private Rectangle[] requestRectangle(){
        Rectangle[] rectangleArray = new Rectangle[each_num];
        Arrays.fill(rectangleArray, null);
        int index = 0;

        requestLock.lock();{
            int copyCurRow = current_row;
            int copyCurCol = current_col;
            log.info("[DISTRIBUTOR]: generate task. row: " + copyCurRow + ", col: " + copyCurCol + ". ");
            int row = copyCurRow;
            outer:
            for(; row < rowNum; row++){
                for(int col = copyCurCol; col < colNum; col++){
                    rectangleArray[index] = createRectangle(row, col);
                    index++;
                    if(index >= each_num){
                        copyCurCol = col + 1;
                        break outer;
                    }
                }
                copyCurCol = 0;
            }
            copyCurRow = row + (copyCurCol == colNum ? 1 : 0);
            copyCurCol = copyCurCol % colNum;

            current_row = copyCurRow;
            current_col = copyCurCol;
        }requestLock.unlock();

        return rectangleArray;
    }

    private void search(){
        for(int i = 0;i < this.totalThreadNumber;i++){
            CompletableFuture.runAsync(() -> {
                outer:
                while(true){
                    while(!statusEnum.equals(StatusEnum.Run)){
                        statusLock.lock();
                        statusLockCondition.awaitUninterruptibly();
                        statusLock.unlock();
                    }

                    Rectangle[] rectangles = this.requestRectangle();
                    for(Rectangle rectangle : rectangles){
                        if(rectangle == null) break outer;
                        this.search(rectangle);
                    }
                }
                System.out.println("[OVER]");
            }, this.executorService)
            .exceptionally(throwable -> {
                System.out.print("error: ");
                throwable.printStackTrace(System.out);
                return null;
            });
        }
    }

    protected abstract void search(Rectangle rectangle);

    protected void commit(TaskData taskData){
        if(taskData != null) this.taskStore.addTask(taskData);
    }

    @Override
    public void doStart(){
        if(statusEnum != StatusEnum.Run){
            statusOpLock.lock();
            statusEnum = StatusEnum.Run;
            statusOpLock.unlock();

            this.search();

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

        this.requestLock.lock();
        this.current_col = 0;
        this.current_row = 0;
        this.requestLock.unlock();
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
