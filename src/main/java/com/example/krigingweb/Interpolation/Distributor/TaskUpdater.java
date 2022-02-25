package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Service.LandService;

import java.util.List;

class TaskUpdater {
    private final LandService landService;

    public TaskUpdater(LandService landService) {
        this.landService = landService;
    }

    public void update(List<LandEntity> landEntityList){
        this.landService.updateLand(landEntityList);
    }
}
