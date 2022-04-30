package com.example.krigingweb.Interpolation.Distributor;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(prefix = "distributor", name = "enable", havingValue = "true")
@Component
@Getter
public class DistributorProperties {
    @Value(value = "${distributor.enable}")
    private boolean enable = false;

    @Value(value = "${distributor.timeoutMinutes}")
    private long timeoutMinutes = 3;

    @Value(value = "${distributor.totalTaskGeneratorThreadNumber}")
    private int totalTaskGeneratorThreadNumber = 1;

    @Value(value = "${distributor.totalTaskUpdaterThreadNumber}")
    private int totalTaskUpdaterThreadNumber = 1;

    @Value(value = "${distributor.totalTaskDistributorPostThreadNumber}")
    private int totalTaskDistributorPostThreadNumber = 1;

    @Value(value = "${distributor.taskStoreMaxCount}")
    private int taskStoreMaxCount = 20;

    @Autowired
    public DistributorProperties(){}
}
