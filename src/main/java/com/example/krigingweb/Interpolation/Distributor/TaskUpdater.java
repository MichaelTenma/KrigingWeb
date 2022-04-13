package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Interpolation.Distributor.Exception.UpdateException;
import com.example.krigingweb.Service.LandService;

import java.util.List;

class TaskUpdater {
    private final LandService landService;

    public TaskUpdater(LandService landService) {
        this.landService = landService;
    }

    public void update(List<LandEntity> landEntityList) throws UpdateException {
        try{
            this.landService.updateLand(landEntityList);
        }catch (Exception e){
            throw new UpdateException(e);
        }
    }

}
