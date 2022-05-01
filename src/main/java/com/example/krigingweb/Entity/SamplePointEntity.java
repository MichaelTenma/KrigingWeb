package com.example.krigingweb.Entity;

import com.example.krigingweb.Serializer.DoubleSerializer;
import com.example.krigingweb.Serializer.GeometryJsonConverter;
import com.example.krigingweb.Serializer.UUIDJsonConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SamplePointEntity implements Serializable {

    @JsonSerialize(using = UUIDJsonConverter.Serializer.class)
    @JsonDeserialize(using = UUIDJsonConverter.Deserializer.class)
    private UUID id;

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

    @JsonSerialize(using = DoubleSerializer.class)
    private Double distance;

}
