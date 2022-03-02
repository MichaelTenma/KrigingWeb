package com.example.krigingweb.Configuration;

import com.example.krigingweb.Serializer.UUIDJsonConverter;
import com.example.krigingweb.Serializer.ZonedDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.ZonedDateTime;
import java.util.UUID;

@Configuration
public class JsonConfig {

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        SimpleModule simpleModule = new SimpleModule();

//        simpleModule.addSerializer(Point.class, new PointJsonSerializer());
//        simpleModule.addSerializer(Point.class, new GeometryJsonConverter.Serializer<>());
//        simpleModule.addDeserializer(Point.class, new GeometryJsonConverter.Deserializer<>());
//        simpleModule.addSerializer(MultiPoint.class, new GeometryJsonConverter.Serializer<>());
//        simpleModule.addDeserializer(MultiPoint.class, new GeometryJsonConverter.Deserializer<>());
//
//        simpleModule.addSerializer(Polygon.class, new GeometryJsonConverter.Serializer<>());
//        simpleModule.addDeserializer(Polygon.class, new GeometryJsonConverter.Deserializer<>());
//        simpleModule.addSerializer(MultiPolygon.class, new GeometryJsonConverter.Serializer<>());
//        simpleModule.addDeserializer(MultiPolygon.class, new GeometryJsonConverter.Deserializer<>());
//
//        simpleModule.addSerializer(LinearRing.class, new GeometryJsonConverter.Serializer<>());
//        simpleModule.addDeserializer(LinearRing.class, new GeometryJsonConverter.Deserializer<>());
//
//        simpleModule.addSerializer(MultiLineString.class, new GeometryJsonConverter.Serializer<>());
//        simpleModule.addDeserializer(MultiLineString.class, new GeometryJsonConverter.Deserializer<>());
//
//        simpleModule.addSerializer(MultiLineString.class, new GeometryJsonConverter.Serializer<>());
//        simpleModule.addDeserializer(MultiLineString.class, new GeometryJsonConverter.Deserializer<>());

        simpleModule.addSerializer(UUID.class, new UUIDJsonConverter.Serializer());
        simpleModule.addDeserializer(UUID.class, new UUIDJsonConverter.Deserializer());

        simpleModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());


        objectMapper.registerModule(simpleModule);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}
