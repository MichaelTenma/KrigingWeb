package com.example.krigingweb.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.MultiPolygon;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class LandEntity {
    private UUID landId;
    private MultiPolygon multiPolygon;

    private NutrientEntity pH;
    private NutrientEntity OC;
    private NutrientEntity N;
    private NutrientEntity P;
    private NutrientEntity K;

    public LandEntity(UUID landId, MultiPolygon multiPolygon) {
        this.landId = landId;
        this.multiPolygon = multiPolygon;
    }
}
