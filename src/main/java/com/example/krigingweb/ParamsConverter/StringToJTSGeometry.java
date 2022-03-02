package com.example.krigingweb.ParamsConverter;

import com.example.krigingweb.Interpolation.Core.Util.GeoUtil;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.springframework.core.convert.converter.Converter;

public class StringToJTSGeometry <T extends Geometry> implements Converter<String, T> {
    public T convert(String source) {
        try {
            return (T)GeoUtil.wktReader.read(source);
        } catch (ParseException e) {
            return null;
        }
    }
}
