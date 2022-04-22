package com.example.krigingweb.Interpolation.Distributor.TaskGenerator;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Interpolation.Core.Enum.InterpolatedStatusEnum;
import com.example.krigingweb.Interpolation.Core.TaskData;
import com.example.krigingweb.Interpolation.Distributor.Core.Rectangle;
import com.example.krigingweb.Interpolation.Distributor.TaskStore;
import com.example.krigingweb.Service.LandService;
import com.example.krigingweb.Service.SamplePointService;

import java.util.List;

public class RectangleQuickBufferTaskGenerator extends AbstractTaskGenerator {
    private final LandService landService;
    private final SamplePointService samplePointService;

    public RectangleQuickBufferTaskGenerator(
            TaskStore taskStore, int totalThreadNumber,
            LandService landService, SamplePointService samplePointService
    ) {
        super(taskStore, totalThreadNumber);
        this.landService = landService;
        this.samplePointService = samplePointService;
    }

    @Override
    protected void search(Rectangle rectangle) {
        /* 取出当前矩形内所有未插值的地块 */
        List<LandEntity> landEntityList = this.landService.list(rectangle, InterpolatedStatusEnum.UnStart);
        if(landEntityList != null && landEntityList.size() > 0){
            final int pointsNum = 200;
            Double predictDistance = this.landService.predictBufferDistance(
                    rectangle, 10000, pointsNum + 100
            );
            double maxDistance = this.landService.calMaxDistance(rectangle, predictDistance, pointsNum);
            List<SamplePointEntity> samplePointEntityList =
                    this.samplePointService.list(rectangle.bufferFromCenter(maxDistance));
            if(samplePointEntityList != null && samplePointEntityList.size() > 0){
                TaskData taskData = new TaskData(samplePointEntityList, landEntityList);
                this.landService.markPrepareInterpolated(landEntityList);
                this.commit(taskData);
            }
        }
    }
}
