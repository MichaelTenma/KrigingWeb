package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Service.LandService;
import com.example.krigingweb.Service.SamplePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@ConditionalOnProperty(prefix = "distributor", name = "enable", havingValue = "true")
@Configuration
@Import({DataSourceAutoConfiguration.class})
public class DistributorConfig {
    private final RestTemplate restTemplate;
    private final LandService landService;
    private final DistributorProperties distributorProperties;

    private final SamplePointService samplePointService;

    @Autowired
    public DistributorConfig(
        RestTemplate restTemplate, LandService landService,
        DistributorProperties distributorProperties, SamplePointService samplePointService
    ) {
        this.restTemplate = restTemplate;
        this.landService = landService;
        this.distributorProperties = distributorProperties;
        this.samplePointService = samplePointService;
    }

    @Bean
    public DistributorManager distributorManager(){
        int totalTaskGeneratorThreadNumber = this.distributorProperties.getTotalTaskGeneratorThreadNumber();
        int totalTaskUpdaterThreadNumber = this.distributorProperties.getTotalTaskUpdaterThreadNumber();
        int totalTaskDistributorPostThreadNumber = this.distributorProperties.getTotalTaskDistributorPostThreadNumber();
        long timeoutMinutes = this.distributorProperties.getTimeoutMinutes();
        int taskStoreMaxCount = this.distributorProperties.getTaskStoreMaxCount();
        return new DistributorManager(
            totalTaskGeneratorThreadNumber, totalTaskUpdaterThreadNumber, totalTaskDistributorPostThreadNumber,
            timeoutMinutes, taskStoreMaxCount, this.landService, this.samplePointService, this.restTemplate
        );
    }

}
