package com.example.krigingweb.Controller;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Exception.EmptyException;
import com.example.krigingweb.Exception.EmptyIPException;
import com.example.krigingweb.Exception.EmptyListException;
import com.example.krigingweb.Interpolation.Core.Enum.CallbackHttpEnum;
import com.example.krigingweb.Interpolation.Core.Util.IPUtil;
import com.example.krigingweb.Interpolation.Distributor.DistributorManager;
import com.example.krigingweb.Interpolation.Distributor.Response.DoneTaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/distributor")
@ResponseBody
@Slf4j
public class DistributorController {

    private final DistributorManager distributorManager;

    @Autowired
    public DistributorController(DistributorManager distributorManager) {
        this.distributorManager = distributorManager;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
        HttpServletRequest httpServletRequest, UUID interpolaterID,
        String url, CallbackHttpEnum callbackHttpEnum
    ) throws EmptyException, EmptyIPException {
        EmptyException.check("interpolaterID", interpolaterID);
        EmptyException.check("url", url);

        if(callbackHttpEnum == null) callbackHttpEnum = CallbackHttpEnum.http;

        String ip = IPUtil.getIpAddr(httpServletRequest);
        EmptyIPException.check(ip);

        url = callbackHttpEnum + "://" + ip + "/" + url;
        this.distributorManager.registerInterpolater(interpolaterID, url);
        log.info(
            "[REGISTER INTERPOLATER]: " +
            String.format("interpolaterID: %s, url: %s", interpolaterID, url)
        );
        return new ResponseEntity<>("插值结点注册成功！", HttpStatus.OK);
    }

    @PostMapping("/doneTask")
    public ResponseEntity<DoneTaskStatus> doneTask(
        UUID taskID, List<LandEntity> landEntityList
    ) throws EmptyException, EmptyListException {
        EmptyException.check("taskID", taskID);
        EmptyListException.check("landEntityList", landEntityList);

        DoneTaskStatus doneTaskStatus = this.distributorManager.doneTask(taskID, landEntityList);
        log.info("[DONE TASK]: " + doneTaskStatus);
        return new ResponseEntity<>(doneTaskStatus, HttpStatus.OK);
    }

    @PostMapping("/interpolaterException")
    public ResponseEntity<String> interpolaterException(UUID interpolaterID) throws EmptyException {
        EmptyException.check("interpolaterID", interpolaterID);

        this.distributorManager.deleteInterpolater(interpolaterID);
        log.warn("[EXCEPTIONAL INTERPOLATER]: " + interpolaterID);
        return new ResponseEntity<>("标记插值结点异常成功！", HttpStatus.OK);
    }
}
