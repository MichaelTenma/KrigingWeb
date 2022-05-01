package com.example.krigingweb.Entity;

import com.example.krigingweb.Serializer.DoubleSerializer;
import com.example.krigingweb.Serializer.GeometryJsonConverter;
import com.example.krigingweb.Serializer.UUIDJsonConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.locationtech.jts.geom.MultiPolygon;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LandEntity implements Serializable {
    @JsonProperty("landId")
    @JsonSerialize(using = UUIDJsonConverter.Serializer.class)
    @JsonDeserialize(using = UUIDJsonConverter.Deserializer.class)
    private UUID landId;

    @JsonProperty("geom")
    @JsonSerialize(using = GeometryJsonConverter.Serializer.class)
    @JsonDeserialize(using = GeometryJsonConverter.Deserializer.class)
    private MultiPolygon geom;

    @JsonProperty("pH")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double pH;

    @JsonProperty("OC")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double OC;

    @JsonProperty("N")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double N;

    @JsonProperty("P")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double P;

    @JsonProperty("K")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double K;

    public LandEntity(UUID landId, MultiPolygon geom) {
        this.landId = landId;
        this.geom = geom;
    }

    private static boolean couldBeUpdate(Double nutrient){
        boolean isFail = Double.isNaN(nutrient) || Double.isInfinite(nutrient);
        return !isFail;
    }

    public boolean couldBeUpdate(){
        return
            LandEntity.couldBeUpdate(this.K) &&
            LandEntity.couldBeUpdate(this.N) &&
            LandEntity.couldBeUpdate(this.OC) &&
            LandEntity.couldBeUpdate(this.pH) &&
            LandEntity.couldBeUpdate(this.P)
        ;
    }
}
