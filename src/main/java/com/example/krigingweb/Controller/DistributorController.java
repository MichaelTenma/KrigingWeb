package com.example.krigingweb.Controller;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Interpolation.Distributor.DistributorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/distributor")
@ResponseBody
public class DistributorController {

    private final DistributorManager distributorManager;

    @Autowired
    public DistributorController(DistributorManager distributorManager) {
        this.distributorManager = distributorManager;
    }

    public void register(UUID interpolaterID, String url){
        this.distributorManager.registerInterpolater(interpolaterID, url);
    }

    public void doneTask(UUID taskID, List<LandEntity> landEntityList){
        this.distributorManager.doneTask(taskID, landEntityList);
    }

    public void interpolaterException(UUID interpolaterID){
        this.distributorManager.deleteInterpolater(interpolaterID);
    }
}
