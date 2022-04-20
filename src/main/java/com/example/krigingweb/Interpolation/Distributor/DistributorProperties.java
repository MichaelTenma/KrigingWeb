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

    /* 单个interpolater未完成插值任务的限制数量 */
    @Value(value = "${distributor.undoneTaskLimitPerInterpolater}")
    private int undoneTaskLimitPerInterpolater = 100;

    @Value(value = "${distributor.timeoutMinutes}")
    private long timeoutMinutes = 15;

    @Value(value = "${distributor.totalTaskGeneratorThreadNumber}")
    private int totalTaskGeneratorThreadNumber = 1;

    @Value(value = "${distributor.totalTaskUpdaterThreadNumber}")
    private int totalTaskUpdaterThreadNumber = 2;

    @Autowired
    public DistributorProperties(){}
}
