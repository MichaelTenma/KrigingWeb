package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Interpolation.Distributor.Service.DistributorService;
import com.example.krigingweb.Service.LandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class DistributorConfig {
    private final RestTemplate restTemplate;
    private final LandService landService;
    private final ExecutorService executorService;
    private final DistributorProperties distributorProperties;

    private final ObjectMapper objectMapper;

    @Autowired
    public DistributorConfig(RestTemplate restTemplate, LandService landService, DistributorProperties distributorProperties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.landService = landService;
        this.distributorProperties = distributorProperties;
        this.executorService = Executors.newFixedThreadPool(distributorProperties.getCurrentNumber());
        this.objectMapper = objectMapper;
    }

    @Bean
    public DistributorManager distributorManager(){
        return new DistributorManager(
            this.executorService, this.restTemplate,
            this.landService, this.distributorProperties,
                this.objectMapper
        );
    }

}
