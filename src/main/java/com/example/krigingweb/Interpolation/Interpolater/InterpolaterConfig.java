package com.example.krigingweb.Interpolation.Interpolater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class InterpolaterConfig {
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;

    @Value(value = "${interpolater.currentNumber}")
    private int currentNumber = 2;

    @Autowired
    public InterpolaterConfig(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.executorService = Executors.newFixedThreadPool(this.currentNumber);
    }

    @Bean
    public InterpolaterManager interpolaterManager(){
        return new InterpolaterManager(
            this.executorService, this.restTemplate
        );
    }
}
