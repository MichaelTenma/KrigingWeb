package com.example.krigingweb.Configuration;

import com.example.krigingweb.ParamsConverter.StringToJTSGeometry;
import com.example.krigingweb.ParamsConverter.StringToUUIDConverter;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;

@Configuration
public class ParamsConfig {

    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Autowired
    public ParamsConfig(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
    }

    @PostConstruct
    public void addConversionConfig() {
        ConfigurableWebBindingInitializer initializer =
                (ConfigurableWebBindingInitializer) requestMappingHandlerAdapter.getWebBindingInitializer();

        if (initializer.getConversionService() != null) {
            GenericConversionService genericConversionService =
                    (GenericConversionService)initializer.getConversionService();

            genericConversionService.addConverter(new StringToUUIDConverter());
            genericConversionService.addConverter(new StringToJTSGeometry<Point>());
        }
    }
}
