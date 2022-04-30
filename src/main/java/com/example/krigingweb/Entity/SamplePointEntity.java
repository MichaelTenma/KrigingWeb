package com.example.krigingweb.Entity;

import com.example.krigingweb.Serializer.GeometryJsonConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SamplePointEntity {
    private UUID pointId;

    @JsonSerialize(using = GeometryJsonConverter.Serializer.class)
    @JsonDeserialize(using = GeometryJsonConverter.Deserializer.class)
    private Point geom;

    private Integer time;

    @JsonProperty("SMC")
    private String SMC;
    @JsonProperty("DMC")
    private String DMC;
    @JsonProperty("XMC")
    private String XMC;
    @JsonProperty("YMC")
    private String YMC;
    @JsonProperty("CMC")
    private String CMC;

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

    private Double distance;
}
