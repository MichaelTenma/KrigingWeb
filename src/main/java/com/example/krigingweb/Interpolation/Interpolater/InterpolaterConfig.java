package com.example.krigingweb.Interpolation.Interpolater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.concurrent.*;

@ConditionalOnProperty(prefix = "interpolater", name = "enable", havingValue = "true")
@Configuration
public class InterpolaterConfig {
    private final RestTemplate restTemplate;

    private final InterpolaterProperties interpolaterProperties;

    @Autowired
    public InterpolaterConfig(RestTemplate restTemplate, InterpolaterProperties interpolaterProperties) {
        this.restTemplate = restTemplate;
        this.interpolaterProperties = interpolaterProperties;
    }

    @Bean
    public InterpolaterManager interpolaterManager(){
        return new InterpolaterManager(this.restTemplate, this.interpolaterProperties);
    }
}
