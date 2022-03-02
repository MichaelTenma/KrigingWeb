package com.example.krigingweb.Interpolation.Distributor.Service;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Exception.EmptyException;
import com.example.krigingweb.Exception.EmptyIPException;
import com.example.krigingweb.Exception.EmptyListException;
import com.example.krigingweb.Interpolation.Basic.Enum.CallbackHttpEnum;
import com.example.krigingweb.Interpolation.Basic.IPUtil;
import com.example.krigingweb.Interpolation.Distributor.Response.DoneTaskStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DistributorService {
    void register(
        HttpServletRequest httpServletRequest,
        UUID interpolaterID,
        String apiPath,
        String port,
        CallbackHttpEnum callbackHttpEnum
    ) throws EmptyException, EmptyIPException;

    DoneTaskStatus doneTask(UUID taskID, List<LandEntity> landEntityList) throws EmptyException, EmptyListException;

    void interpolaterException(UUID interpolaterID) throws EmptyException;

    Map<UUID, String> showInterpolaterURLMap();

    void start();
}
