package com.example.krigingweb.Serializer;

import com.example.krigingweb.Interpolation.Core.Util.GeoUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public final class GeometryJsonConverter {
    public static class Serializer<T extends Geometry> extends JsonSerializer<T>{
        @Override
        public void serialize(T geometry, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeBinary(GeoUtil.toBytes(geometry));
        }
    }

    public static class Deserializer<T extends Geometry> extends JsonDeserializer<T>{
        @Override
        public T deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext
        ) throws IOException {
            return GeoUtil.toGeometry(jsonParser.getBinaryValue());
        }
    }
}
