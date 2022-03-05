package com.example.krigingweb.Interpolation.Basic;

import com.example.krigingweb.Interpolation.Basic.Enum.StatusEnum;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class RectangleSearcher implements StatusManage {
    /**
     * bias是用于兼容gcj02与wgs1984以及国家2000大地坐标系之间的差别，
     * 向四周扩宽5公里，保证整个广东在这三种坐标系下都涵盖在大矩形框中
     * 注意不包括东沙群岛，目前测土配方施肥的插值区域仅为广东省陆地区域
     */
    private static final double bias = 5000;
    private static final double x_min = 12207881.844549 - bias;
    private static final double x_max = 13045561.012768 + bias;
    private static final double y_min = 2301216.219071 - bias;
    private static final double y_max = 2939884.095192 + bias;

    private static final int rowNum = 40;// N
    private static final int colNum = 32;// M

    /* 向上取整，避免由于双精度浮点运算带来的舍入误差而导致某些地块不被涵盖在矩形框内 */
    private static final double x_gap = Math.ceil((x_max - x_min) / colNum);
    private static final double y_gap = Math.ceil((y_max - y_min) / rowNum);

    private final ExecutorService executorService;

    public RectangleSearcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public static class Rectangle {
        private final double left;
        private final double right;
        private final double top;
        private final double bottom;

        public Rectangle(double left, double right, double top, double bottom) {
            this.left = left;
            this.right = right;
            this.bottom = bottom;
            this.top = top;
        }

        public Polygon toPolygon(GeometryFactory geometryFactory){
            return geometryFactory.createPolygon(
                new Coordinate[]{
                    new Coordinate(left, bottom),
                    new Coordinate(right, bottom),
                    new Coordinate(right, top),
                    new Coordinate(left, top),
                    new Coordinate(left, bottom)
                }
            );
        }

        public double getArea(){
            return (this.right - this.left) * (this.top - this.bottom);
        }

        public Rectangle buffer(double distance){
            return new Rectangle(
                this.left - distance,
                this.right + distance,
                this.top + distance,
                this.bottom - distance
            );
        }

        public Rectangle bufferFromCenter(double distance){
            double x_center = (this.right - this.left) / 2 + this.left;
            double y_center = (this.top - this.bottom) / 2 + this.bottom;
            return new Rectangle(
                x_center - distance,
                x_center + distance,
                y_center + distance,
                y_center - distance
            );
        }

        @Override
        public String toString() {
            return String.format(
                "Polygon((%f %f,%f %f,%f %f,%f %f,%f %f))",
                left, bottom, /* 左下角 */
                right, bottom, /* 右下角 */
                right, top, /* 右上角 */
                left, top, /* 左上角 */
                left, bottom/* 左下角 */
            );
        }
    }

    @FunctionalInterface
    public interface LandSearcher {
        /**
         * 用于编写搜索地块的SQL相关代码，避免Interpolation库与数据库耦合
         * @param rectangle 搜索矩形
         */
        void search(Rectangle rectangle);
    }

    /**
     * 将广东省划分成 N*M 份，注意从左下角开始
     * @param row 行，从0开始，[0, N)
     * @param col 列，从0开始，[0, M)
     * @return 矩形的WKT字符串
     */
    private Rectangle createRectangle(int row, int col){
        if(
            row >= rowNum || row < 0 ||
            col >= colNum || col < 0
        ) return null;

        final double left = x_min + col * x_gap;
        final double right = left + x_gap;
        final double bottom = y_min + row * y_gap;
        final double top = bottom + y_gap;
        return new Rectangle(left, right, top, bottom);
    }

    private int beginRow = 0;
    private int beginCol = 0;
    private StatusEnum statusEnum = StatusEnum.Stop;

    @Override
    public void start(){ this.statusEnum = StatusEnum.Run; }

    @Override
    public void pause(){ this.statusEnum = StatusEnum.Pause; }

    @Override
    public void resume() {
        this.start();
    }

    @Override
    public void stop(){ this.statusEnum = StatusEnum.Stop; }

    public StatusEnum getStatusEnum() {
        return this.statusEnum;
    }

    public void reset(){
        this.beginRow = 0;
        this.beginCol = 0;
        this.stop();
    }

    @Data
    public class BooleanObject{
        private boolean value;
        public BooleanObject(boolean value){
            this.value = value;
        }
    }

    /**
     * 将广东省切分成多份，逐份搜索
     * @param landSearcher 地块搜索器，编写与数据库耦合的代码
     * @return True表示完成所有矩形框的搜索工作，False表示搜索过程中由于某些情况而中止
     */
    public CompletableFuture<BooleanObject> search(LandSearcher landSearcher){
        /* 根据矩形框随机选择一个未插值地块 */
        /* 向外扩展选择约有350个点（一般来说应该多选一些点） */
        /* 每个指标选择350个有效点 */
        /* 生成各指标有效点构成的最小凹多边形 */
        /* 根据多边形生成对角线长度1/20的内缓冲区 */
        /* 各指标缓冲区求交集，得最小缓冲区 */
        /* 查找最小缓冲区内的地块，务必保证缓冲区内至少有一个地块 */
        BooleanObject booleanObject = new BooleanObject(true);
        CompletableFuture<BooleanObject> completableFuture = new CompletableFuture<>();
        outer:
        for(int row = this.beginRow; row < RectangleSearcher.rowNum; row++){
            for(int col = this.beginCol; col < RectangleSearcher.colNum; col++){
                if(!this.statusEnum.equals(StatusEnum.Run)){
                    this.beginRow = row;
                    this.beginCol = col;
                    booleanObject.setValue(false);
                    break outer;
                }

                final int finalRow = row;
                final int finalCol = col;
                this.executorService.submit(() -> {
                    landSearcher.search(this.createRectangle(finalRow, finalCol));
                    if(
                        finalCol == RectangleSearcher.colNum - 1 &&
                        finalRow == RectangleSearcher.rowNum - 1
                    ){
                        completableFuture.complete(booleanObject);
                    }
                });
            }
        }
        return completableFuture;
    }

    public static Rectangle getExtent(){
        return new Rectangle(x_min, x_max, y_max, y_min);
    }

}

