package com.example.krigingweb.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.MultiPolygon;
import java.io.IOException;

public class MultiPolygonSerializer extends JsonSerializer<MultiPolygon> {

    @Override
    public void serialize(MultiPolygon multiPolygon, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(multiPolygon.toString());
    }
}
