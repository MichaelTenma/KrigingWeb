package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Interpolation.Basic.Enum.StatusEnum;
import com.example.krigingweb.Interpolation.Basic.StatusManage;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Distributor.Core.InterpolaterNode;
import com.example.krigingweb.Interpolation.Distributor.Response.DoneTaskStatus;
import com.example.krigingweb.Interpolation.Distributor.TaskGenerator.AbstractTaskGenerator;
import com.example.krigingweb.Interpolation.Distributor.TaskGenerator.RectangleQuickBufferTaskGenerator;
import com.example.krigingweb.Service.LandService;
import com.example.krigingweb.Service.SamplePointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class DistributorManager implements StatusManage {

    private final AbstractTaskGenerator taskGenerator;
    private final TaskStore taskStore;
    private final InterpolaterStore interpolaterStore;
    private final UndoneTaskManager undoneTaskManager;
    private final TaskUpdater taskUpdater;

    private final RestTemplate restTemplate;

    private final Queue<InterpolaterNode> readyQueue = new LinkedList<>();
    private final Queue<InterpolaterNode> runningQueue = new LinkedList<>();

    private final ScheduledExecutorService daemonExecutorService= Executors.newScheduledThreadPool(
        1, new CustomizableThreadFactory("distributor-daemon-")
    );

    private StatusEnum statusEnum = StatusEnum.Stop;
    private final ReentrantLock statusOpLock = new ReentrantLock();

    public DistributorManager(
        int totalTaskGeneratorThreadNumber, int totalTaskUpdaterThreadNumber,
        LandService landService, SamplePointService samplePointService,
        RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
        this.taskStore = new TaskStore(10);
        this.taskGenerator = new RectangleQuickBufferTaskGenerator(
            this.taskStore, totalTaskGeneratorThreadNumber,
            landService, samplePointService
        );

        ExecutorService taskGeneratorExecutorService = Executors.newFixedThreadPool(
            totalTaskGeneratorThreadNumber,
            new CustomizableThreadFactory("distributor-taskGenerator-")
        );
        for(int i = 0;i < totalTaskGeneratorThreadNumber;i++){
            taskGeneratorExecutorService.execute(this.taskGenerator);
        }

        this.interpolaterStore = new InterpolaterStore();
        this.undoneTaskManager = new UndoneTaskManager();
        this.taskUpdater = new TaskUpdater(totalTaskUpdaterThreadNumber, landService);
    }

    public void heartBeat(UUID interpolaterID){
        this.interpolaterStore.heartBeat(interpolaterID);
    }

    public void registerInterpolater(UUID interpolaterID, int maxTaskNumber, String url){
        InterpolaterNode interpolaterNode = new InterpolaterNode(interpolaterID, maxTaskNumber, url);
        this.interpolaterStore.registerInterpolater(interpolaterNode);
        this.readyQueue.add(interpolaterNode);
    }

    public void deleteInterpolater(UUID interpolaterID){
        this.interpolaterStore.deleteInterpolater(interpolaterID);
    }

    private void runTask(InterpolaterNode interpolaterNode, TaskData taskData){
        taskData.belongInterpolaterID = interpolaterNode.id;
        taskData.updatePostTime();
        /* 待完成任务存储器 */
        this.undoneTaskManager.addUndoneTask(taskData);
        /* 发出任务 */
        this.daemonExecutorService.submit(
            () -> {
                try{
                    interpolaterNode.addTask(taskData, this.restTemplate);
                }catch (Throwable e){
                    System.out.println("runTask");
                    e.printStackTrace();
                }
            }
        );
    }
    private void requestTask(){
        /* 是否有任务，是否有空闲结点，若有则申请任务，否则进入下一轮等待 */
        while(!this.readyQueue.isEmpty() && !this.taskStore.isEmpty()){
            InterpolaterNode interpolaterNode = this.readyQueue.poll();
            if(interpolaterNode == null) break;
            if(!this.interpolaterStore.hasInterpolater(interpolaterNode.id)) continue;

            while(!this.taskStore.isEmpty() && interpolaterNode.getRestTaskNumber() > 0){
                TaskData taskData = this.taskStore.requestTask();
                if(taskData != null)this.runTask(interpolaterNode, taskData);
            }

            if(!this.runningQueue.contains(interpolaterNode)) {
                this.runningQueue.add(interpolaterNode);
            }

            if(interpolaterNode.getRestTaskNumber() > 0 && !this.readyQueue.contains(interpolaterNode)){
                this.readyQueue.add(interpolaterNode);
            }
        }
    }

    private void timeout(TaskData taskData){
        this.interpolaterStore.working(taskData.belongInterpolaterID);
        if(!taskData.couldBeDistributed()) return;
        /* 应该立即重新分发任务执行 */
        InterpolaterNode interpolaterNode = this.interpolaterStore.getRandomInterpolater();
        if(interpolaterNode != null){
            this.runTask(interpolaterNode, taskData);
        }else{
            /* 没有可以使用的插值结点，只能放入待完成任务管理器中，等待有插值结点注册后的下一轮超时 */
            taskData.belongInterpolaterID = null;
            taskData.updatePostTime();
            this.undoneTaskManager.addUndoneTask(taskData);
        }
    }

    /**
     * 完成该插值任务，并向数据库更新
     * @param taskID 插值任务ID
     * @param landEntityList 插值后的地块结果
     * @return 整个插值任务花费的秒数
     */
    public DoneTaskStatus doneTask(UUID taskID, List<LandEntity> landEntityList){
        /* null: 任务在超时后完成，undoneTaskManager没有该任务，自然就是null */
        TaskData taskData = this.undoneTaskManager.doneTask(taskID);
        if(taskData == null) return null;
        this.taskUpdater.update(landEntityList)
            .thenRun(() -> {
                log.info("[DONE TASK]: " + new DoneTaskStatus(taskData));
            })
            .exceptionally(throwable -> {
                /* 放弃taskData的更新 */
                log.error("[DISTRIBUTOR]: taskID: " + taskID + "更新失败！");
//                this.undoneTaskManager.addUndoneTask(taskData);
                return null;
            })
            .thenRun(() -> {
                InterpolaterNode interpolaterNode = this.interpolaterStore.working(taskData.belongInterpolaterID);
                if(interpolaterNode.maxTaskNumber == interpolaterNode.getRestTaskNumber()){
                    this.runningQueue.remove(interpolaterNode);
                }
                if(!this.readyQueue.contains(interpolaterNode)){
                    this.readyQueue.add(interpolaterNode);
                }
            });
        return new DoneTaskStatus(taskData);
    }

    @Override
    public void doStart() {
        if(statusEnum != StatusEnum.Run){
            statusOpLock.lock();
            statusEnum = StatusEnum.Run;
            statusOpLock.unlock();
            this.taskGenerator.doStart();

            /* 单线程定时执行 */
            this.daemonExecutorService.scheduleAtFixedRate(() -> {
                try{
                    if(statusEnum == StatusEnum.Run){
                        /* 心跳检测 */
                        this.interpolaterStore.heartBeatDetected();
                        /* 超时检测 */
                        this.undoneTaskManager.timeout(3, this::timeout);
                        /* 任务调度 */
                        this.requestTask();
                    }
                }catch (Throwable e){
                    System.out.println("doStart");
                    e.printStackTrace();
                }
            }, 0, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void doPause() {
        statusOpLock.lock();
        statusEnum = StatusEnum.Pause;
        statusOpLock.unlock();
        this.taskGenerator.doPause();
    }

    @Override
    public void doResume() {
        this.doStart();
    }

    @Override
    public void doStop() {
        statusOpLock.lock();
        statusEnum = StatusEnum.Stop;
        statusOpLock.unlock();
        this.taskGenerator.doStop();
    }

    public Map<UUID, InterpolaterNode> getInterpolaterNodeMap(){
        return this.interpolaterStore.getInterpolaterNodeMap();
    }
}
