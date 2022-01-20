package com.example.krigingweb.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class SamplePointEntity {
    private UUID point_id;
    private Point geom;
    private Integer time;
    private String SMC;
    private String DMC;
    private String XMC;
    private String YMC;
    private String CMC;

    private Double pH;
    private Double OC;
    private Double N;
    private Double P;
    private Double K;

    private Double distance;
}
