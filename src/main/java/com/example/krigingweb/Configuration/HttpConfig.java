package com.example.krigingweb.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Configuration
public class HttpConfig {

    @Bean
    public RestTemplate restTemplate(
        MappingJackson2HttpMessageConverter httpMessageConverter,
        ObjectMapper objectMapper
    ){
        httpMessageConverter.setObjectMapper(objectMapper);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(httpMessageConverter);
        return restTemplate;
    }
}
