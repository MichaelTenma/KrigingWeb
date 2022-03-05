package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Service.LandService;
import com.example.krigingweb.Service.SamplePointService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ConditionalOnProperty(prefix = "distributor", name = "enable", havingValue = "true")
@Configuration
@Import({DataSourceAutoConfiguration.class})
public class DistributorConfig {
    private final RestTemplate restTemplate;
    private final LandService landService;
    private final ExecutorService executorService;
    private final DistributorProperties distributorProperties;

    private final SamplePointService samplePointService;

    @Autowired
    public DistributorConfig(RestTemplate restTemplate, LandService landService, DistributorProperties distributorProperties, SamplePointService samplePointService) {
        this.restTemplate = restTemplate;
        this.landService = landService;
        this.distributorProperties = distributorProperties;
        this.executorService = Executors.newFixedThreadPool(distributorProperties.getCurrentNumber());
        this.samplePointService = samplePointService;
    }

    @Bean
    public DistributorManager distributorManager(){
        return new DistributorManager(
            this.executorService, this.restTemplate,
            this.landService, this.distributorProperties,
            this.samplePointService
        );
    }

}
