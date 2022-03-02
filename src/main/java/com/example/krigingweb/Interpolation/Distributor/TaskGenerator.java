package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Enum.InterpolatedStatusEnum;
import com.example.krigingweb.Interpolation.Basic.Enum.StatusEnum;
import com.example.krigingweb.Interpolation.Basic.RectangleSearcher;
import com.example.krigingweb.Interpolation.Basic.StatusManage;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Service.LandService;
import com.example.krigingweb.Service.SamplePointService;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

class TaskGenerator implements StatusManage {
    private final TaskStore taskStore;
    private final RectangleSearcher rectangleSearcher;

    private final LandService landService;

    @Setter
    private DoneHandler doneHandler;

    @FunctionalInterface
    public interface DoneHandler{
        void done();
    }

    public TaskGenerator(
        TaskStore taskStore, LandService landService, ExecutorService executorService
    ) {
        this.taskStore = taskStore;
        this.rectangleSearcher = new RectangleSearcher(executorService);
        this.landService = landService;
    }

    /**
     * 将广东省切分成多份，逐份搜索
     * @return True表示完成所有矩形框的搜索工作，False表示搜索过程中由于某些情况而中止
     */
    private void search(){
        CompletableFuture<RectangleSearcher.BooleanObject> completableFuture =
            this.rectangleSearcher.search((RectangleSearcher.Rectangle rectangle) -> {
                /* 根据矩形框随机选择一个未插值地块 */
                UUID landID = this.landService.getRandomLand(rectangle);
                if(landID != null){
                    final int pointsNum = 300;
                    /* 向外扩展选择约有350个点（一般来说应该多选一些点） */
                    Double predictDistance = this.landService.predictBufferDistance(
                        landID, 10000, pointsNum + 150
                    );

                    if(predictDistance != null){
                        /* 每个指标选择350个有效点 */
                        double maxDistance = this.landService.calMaxDistance(landID, predictDistance, pointsNum);

                        /* 取采样点 */
                        List<SamplePointEntity> samplePointEntityList =
                                this.landService.getSamplePointEntityList(landID, maxDistance);

                        /* 生成各指标有效点构成的最小凸包多边形 */
                        /* 各指标有效点构成的最小凹多边形实际上是很类似的，同时有效点是随机分布的，取最大距离构成的缓冲区即可
                         * 若确实害怕无效点聚集在边界部分，则将最大距离缩小1/10避免选择的地块超出有效点内部范围
                         */
                        Geometry convexHull = SamplePointService.getConvexHull(samplePointEntityList);

                        /* 根据最长距离的1/10生成缓冲区 */
                        double correctDistance = -maxDistance * 0.5;
                        Geometry buffer = convexHull.buffer(correctDistance);
                        List<LandEntity> landEntityList = this.landService.list(buffer, InterpolatedStatusEnum.UnStart);
                        this.landService.markPrepareInterpolated(landEntityList);

                        TaskData taskData = new TaskData(samplePointEntityList, landEntityList);
                        this.taskStore.addTask(taskData);

        //                    /* 各指标缓冲区求交集，得最小缓冲区 */
        //                    /* 查找最小缓冲区内的地块，务必保证缓冲区内至少有一个地块 */
                    }
                }
            });

        completableFuture.thenAccept(booleanObject -> {
            if(booleanObject.isValue()){
                /* done callback */
                if(this.doneHandler != null){
                    this.taskStore.commitRest();
                    this.rectangleSearcher.reset();
                    this.doneHandler.done();
                }
            }else{}
        });
    }

    /**
     * 随机获取一个未插值地块，同时按一定容差选取附近地块，以这片地块为中心选择至少500个点进行插值
     */
    @Override
    public void start(){
        /* 同一个对象只能start一次 */
        if(this.rectangleSearcher.getStatusEnum().equals(StatusEnum.Run)) return;
        this.rectangleSearcher.start();
        this.search();
    }

    @Override
    public void pause() {
        this.rectangleSearcher.pause();
    }

    /**
     * 只有当处于暂停状态才会恢复
     */
    @Override
    public void resume() {
        if(this.rectangleSearcher.getStatusEnum() == StatusEnum.Pause){
            this.rectangleSearcher.resume();
            this.search();
        }
    }

    @Override
    public void stop() {
        this.rectangleSearcher.stop();
    }
}
