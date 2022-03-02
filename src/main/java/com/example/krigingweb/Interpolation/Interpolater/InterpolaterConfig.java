package com.example.krigingweb.Interpolation.Interpolater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class InterpolaterConfig {
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;

    private final InterpolaterProperties interpolaterProperties;

    @Autowired
    public InterpolaterConfig(RestTemplate restTemplate, InterpolaterProperties interpolaterProperties) {
        this.restTemplate = restTemplate;
        this.interpolaterProperties = interpolaterProperties;
        this.executorService = Executors.newFixedThreadPool(this.interpolaterProperties.getCurrentNumber());
    }

    @Bean
    public InterpolaterManager interpolaterManager(){
        return new InterpolaterManager(
            this.executorService, this.restTemplate, this.interpolaterProperties
        );
    }
}
