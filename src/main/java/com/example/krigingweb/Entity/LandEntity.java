package com.example.krigingweb.Entity;

import com.example.krigingweb.Serializer.GeometryJsonConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.locationtech.jts.geom.MultiPolygon;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LandEntity {
    @JsonProperty("landId")
    private UUID landId;

    @JsonProperty("multiPolygon")
    @JsonSerialize(using = GeometryJsonConverter.Serializer.class)
    @JsonDeserialize(using = GeometryJsonConverter.Deserializer.class)
    private MultiPolygon multiPolygon;

    @JsonProperty("pH")
    private Double pH;
    @JsonProperty("OC")
    private Double OC;
    @JsonProperty("N")
    private Double N;
    @JsonProperty("P")
    private Double P;
    @JsonProperty("K")
    private Double K;

    public LandEntity(UUID landId, MultiPolygon multiPolygon) {
        this.landId = landId;
        this.multiPolygon = multiPolygon;
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
