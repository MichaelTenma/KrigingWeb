package com.example.krigingweb.Serializer;

import com.example.krigingweb.Interpolation.Core.Util.GeoUtil;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.UUID;

public final class UUIDJsonConverter {
    public static class Serializer extends JsonSerializer<UUID> {
        @Override
        public void serialize(UUID uuid, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(uuid.toString());
        }
    }

    public static class Deserializer extends JsonDeserializer<UUID> {
        @Override
        public UUID deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext
        ) throws IOException {
            return UUID.fromString(jsonParser.getValueAsString());
        }
    }
}
