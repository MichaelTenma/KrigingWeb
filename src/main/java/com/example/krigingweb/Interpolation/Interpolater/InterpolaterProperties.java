package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Basic.Enum.CallbackHttpEnum;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class InterpolaterProperties {
    @Value("${interpolater.distributorURL}")
    private String distributorURL;

    @Value("${interpolater.callbackHttpEnum}")
    private CallbackHttpEnum callbackHttpEnum;

    @Value("${server.port}")
    private String port;

    @Value("${interpolater.enable}")
    private boolean enable = false;

    @Value("${interpolater.currentNumber}")
    private int currentNumber = 2;

    @Value("${interpolater.cellSize}")
    private double cellSize = 300;

    @Value("${interpolater.maxInvalidNum}")
    private int maxInvalidNum = 1;

    @Autowired
    public InterpolaterProperties(){}

}
