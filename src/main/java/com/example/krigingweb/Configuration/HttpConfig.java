package com.example.krigingweb.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Configuration
public class HttpConfig {

    @Bean
    public RestTemplate restTemplate(
        MappingJackson2HttpMessageConverter httpMessageConverter,
        ObjectMapper objectMapper
    ){
        httpMessageConverter.setObjectMapper(objectMapper);
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverterList = restTemplate.getMessageConverters();
        messageConverterList.add(httpMessageConverter);
        return restTemplate;
    }
}
