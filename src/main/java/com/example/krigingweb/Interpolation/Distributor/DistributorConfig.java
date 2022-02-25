package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Service.LandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class DistributorConfig {
    private final RestTemplate restTemplate;
    private final LandService landService;
    private final ExecutorService executorService;

    @Value(value = "${distributor.currentNumber}")
    private int currentNumber = 1;

    @Autowired
    public DistributorConfig(RestTemplate restTemplate, LandService landService) {
        this.restTemplate = restTemplate;
        this.landService = landService;
        this.executorService = Executors.newFixedThreadPool(this.currentNumber);
    }

    @Bean
    public DistributorManager distributorManager(){
        return new DistributorManager(
            this.executorService, this.restTemplate, this.landService
        );
    }
}
