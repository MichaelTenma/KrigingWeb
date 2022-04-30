package com.example.krigingweb.Interpolation.Interpolater;

import com.example.krigingweb.Interpolation.Basic.Enum.CallbackHttpEnum;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(prefix = "interpolater", name = "enable", havingValue = "true")
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

    @Value("${interpolater.concurrentNumber}")
    private int concurrentNumber = 1;

    @Value("${interpolater.cellSize}")
    private double cellSize = 300;

    @Autowired
    public InterpolaterProperties(){}

}
