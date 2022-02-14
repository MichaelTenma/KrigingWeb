package com.example.krigingweb.Configuration;

import com.example.krigingweb.Serializer.MultiPolygonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfig {

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(MultiPolygon.class, new MultiPolygonSerializer());

        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}
