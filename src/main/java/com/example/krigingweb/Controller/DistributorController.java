package com.example.krigingweb.Controller;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Exception.EmptyException;
import com.example.krigingweb.Exception.EmptyIPException;
import com.example.krigingweb.Exception.EmptyListException;
import com.example.krigingweb.Interpolation.Basic.Enum.CallbackHttpEnum;
import com.example.krigingweb.Interpolation.Basic.IPUtil;
import com.example.krigingweb.Interpolation.Distributor.DistributorManager;
import com.example.krigingweb.Interpolation.Distributor.Response.DoneTaskStatus;
import com.example.krigingweb.Request.DoneTaskRequest;
import com.example.krigingweb.Request.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/distributor")
@Slf4j
public class DistributorController {

    private final DistributorManager distributorManager;

    @Autowired
    public DistributorController(DistributorManager distributorManager) {
        this.distributorManager = distributorManager;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
        HttpServletRequest httpServletRequest,
        @RequestBody RegisterRequest registerRequest
    ) throws EmptyException, EmptyIPException {
        EmptyException.check("interpolaterID", registerRequest.interpolaterID);
        EmptyException.check("apiPath", registerRequest.apiPath);

        if(registerRequest.callbackHttpEnum == null) registerRequest.callbackHttpEnum = CallbackHttpEnum.http;

        String ip = IPUtil.getIpAddr(httpServletRequest);
        EmptyIPException.check(ip);

        String url = registerRequest.callbackHttpEnum + "://" + ip + ":" + registerRequest.port + registerRequest.apiPath;
        this.distributorManager.registerInterpolater(registerRequest.interpolaterID, url);
        log.info(
            "[REGISTER INTERPOLATER]: " +
            String.format("interpolaterID: %s, url: %s", registerRequest.interpolaterID, url)
        );
        return new ResponseEntity<>("插值结点注册成功！", HttpStatus.OK);
    }

    @PostMapping("/doneTask")
    public ResponseEntity<DoneTaskStatus> doneTask(
        @RequestBody DoneTaskRequest doneTaskRequest
    ) throws EmptyException, EmptyListException {
        EmptyException.check("taskID", doneTaskRequest.taskID);
        EmptyListException.check("landEntityList", doneTaskRequest.landEntityList);

        DoneTaskStatus doneTaskStatus = this.distributorManager.doneTask(
            doneTaskRequest.taskID, doneTaskRequest.landEntityList
        );
        log.info("[DONE TASK]: " + doneTaskStatus);
        return new ResponseEntity<>(doneTaskStatus, HttpStatus.OK);
    }

    @PostMapping("/interpolaterException")
    public ResponseEntity<String> interpolaterException(
        @RequestParam UUID interpolaterID
    ) throws EmptyException {
        EmptyException.check("interpolaterID", interpolaterID);

        this.distributorManager.deleteInterpolater(interpolaterID);
        log.warn("[EXCEPTIONAL INTERPOLATER]: " + interpolaterID);
        return new ResponseEntity<>("标记插值结点异常成功！", HttpStatus.OK);
    }

    @GetMapping("/showInterpolaterURLMap")
    public ResponseEntity<Map<UUID, String>> showInterpolaterURLMap(){
        return new ResponseEntity<>(this.distributorManager.getInterpolaterURLMap(), HttpStatus.OK);
    }

    @GetMapping("/start")
    public ResponseEntity<String> start(){
        this.distributorManager.start();
        return new ResponseEntity<>("start", HttpStatus.OK);
    }
}
