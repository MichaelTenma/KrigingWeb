package com.example.krigingweb.Interpolation.Distributor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(prefix = "distributor", name = "enable", havingValue = "true")
@Component
public class DistributorProperties {
    @Value(value = "${distributor.enable}")
    private boolean enable = false;

    /* 单个interpolater未完成插值任务的限制数量 */
    @Value(value = "${distributor.undoneTaskLimitPerInterpolater}")
    private int undoneTaskLimitPerInterpolater = 100;

    @Value(value = "${distributor.timeoutMinutes}")
    private long timeoutMinutes = 15;

    @Value(value = "${distributor.currentNumber}")
    private int currentNumber = 1;

    public static final int gapNum = 1;

    @Autowired
    public DistributorProperties(){}

    public boolean isEnable() {
        return enable;
    }

    public int getUndoneTaskLimitPerInterpolater() {
        return undoneTaskLimitPerInterpolater;
    }

    public long getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public int getCurrentNumber() {
        return currentNumber;
    }
}
