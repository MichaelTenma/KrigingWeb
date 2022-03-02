package com.example.krigingweb.Entity;

import com.example.krigingweb.Serializer.GeometryJsonConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class LandEntity {
    @JsonProperty("landId")
    private UUID landId;

    @JsonProperty("multiPolygon")
    @JsonSerialize(using = GeometryJsonConverter.Serializer.class)
    @JsonDeserialize(using = GeometryJsonConverter.Deserializer.class)
    private MultiPolygon multiPolygon;

    @JsonProperty("pH")
    private NutrientEntity pH;
    @JsonProperty("OC")
    private NutrientEntity OC;
    @JsonProperty("N")
    private NutrientEntity N;
    @JsonProperty("P")
    private NutrientEntity P;
    @JsonProperty("K")
    private NutrientEntity K;

    public LandEntity(UUID landId, MultiPolygon multiPolygon) {
        this.landId = landId;
        this.multiPolygon = multiPolygon;
    }

    public boolean couldBeUpdate(){
        return
            this.K.couldBeUpdate() &&
            this.N.couldBeUpdate() &&
            this.OC.couldBeUpdate() &&
            this.pH.couldBeUpdate() &&
            this.P.couldBeUpdate()
        ;
    }
}
