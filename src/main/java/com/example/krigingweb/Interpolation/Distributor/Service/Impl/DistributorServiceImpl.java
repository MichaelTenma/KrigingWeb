package com.example.krigingweb.Interpolation.Distributor.Service.Impl;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Exception.EmptyException;
import com.example.krigingweb.Exception.EmptyIPException;
import com.example.krigingweb.Exception.EmptyListException;
import com.example.krigingweb.Interpolation.Basic.Enum.CallbackHttpEnum;
import com.example.krigingweb.Interpolation.Basic.IPUtil;
import com.example.krigingweb.Interpolation.Distributor.DistributorManager;
import com.example.krigingweb.Interpolation.Distributor.Response.DoneTaskStatus;
import com.example.krigingweb.Interpolation.Distributor.Service.DistributorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class DistributorServiceImpl implements DistributorService {
    private final DistributorManager distributorManager;

    @Autowired
    public DistributorServiceImpl(DistributorManager distributorManager) {
        this.distributorManager = distributorManager;
    }

    @Override
    public void register(
        HttpServletRequest httpServletRequest, UUID interpolaterID,
        String apiPath, String port, CallbackHttpEnum callbackHttpEnum
    ) throws EmptyException, EmptyIPException {
        EmptyException.check("interpolaterID", interpolaterID);
        EmptyException.check("apiPath", apiPath);

        if(callbackHttpEnum == null) callbackHttpEnum = CallbackHttpEnum.http;

        String ip = IPUtil.getIpAddr(httpServletRequest);
        EmptyIPException.check(ip);

        String url = callbackHttpEnum + "://" + ip + ":" + port + apiPath;
        this.distributorManager.registerInterpolater(interpolaterID, url);
        log.info(
            "[REGISTER INTERPOLATER]: " +
            String.format("interpolaterID: %s, url: %s", interpolaterID, url)
        );
    }

    @Override
    public DoneTaskStatus doneTask(
        UUID taskID, List<LandEntity> landEntityList
    ) throws EmptyException, EmptyListException {
        EmptyException.check("taskID", taskID);
        EmptyListException.check("landEntityList", landEntityList);

        DoneTaskStatus doneTaskStatus = this.distributorManager.doneTask(taskID, landEntityList);
        log.info("[DONE TASK]: " + doneTaskStatus);
        return doneTaskStatus;
    }

    @Override
    public void interpolaterException(UUID interpolaterID) throws EmptyException {
        EmptyException.check("interpolaterID", interpolaterID);

        this.distributorManager.deleteInterpolater(interpolaterID);
        log.warn("[EXCEPTIONAL INTERPOLATER]: " + interpolaterID);
    }

    @Override
    public Map<UUID, String> showInterpolaterURLMap(){
        return this.distributorManager.getInterpolaterURLMap();
    }

    @Override
    public void start(){
        this.distributorManager.start();
    }
}
