package com.example.krigingweb.Interpolation;

import com.example.krigingweb.Entity.SamplePointEntity;
import com.example.krigingweb.Enum.SoilNutrientEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class NutrientFilter {
    private static final Map<SoilNutrientEnum, Predicate<SamplePointEntity>> predicateMap = new HashMap<>();
    static {
        predicateMap.put(SoilNutrientEnum.N, s -> s.getN() > 0 && s.getN() < 250);
        predicateMap.put(SoilNutrientEnum.P, s -> s.getP() > 0 && s.getP() < 80);
        predicateMap.put(SoilNutrientEnum.K, s -> s.getK() > 0 && s.getK() < 300);
        predicateMap.put(SoilNutrientEnum.PH, s -> s.getPH() > 0 && s.getPH() < 14);
        predicateMap.put(SoilNutrientEnum.OC, s -> s.getOC() > 0 && s.getOC() < 80);
    }

    public static Predicate<SamplePointEntity> get(SoilNutrientEnum soilNutrientEnum){
        return NutrientFilter.predicateMap.get(soilNutrientEnum);
    }
}
