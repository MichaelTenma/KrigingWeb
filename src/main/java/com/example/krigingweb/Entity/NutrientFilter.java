package com.example.krigingweb.Entity;

import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class NutrientFilter {
    private static final Map<SoilNutrientEnum, Predicate<SamplePointEntity>> predicateMap = new HashMap<>();
    static {
        for(SoilNutrientEnum soilNutrientEnum : SoilNutrientEnum.values()){
            try {
                final Method getSoilNutrientMethod =
                        SamplePointEntity.class.getMethod("get" + soilNutrientEnum);

                predicateMap.put(soilNutrientEnum, s -> {
                    Double nutrient;
                    try {
                        nutrient = (Double) getSoilNutrientMethod.invoke(s);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        nutrient = null;
                    }
                    return nutrient != null
                            && nutrient > soilNutrientEnum.leftRange
                            && nutrient < soilNutrientEnum.rightRange;
                });
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

//        predicateMap.put(SoilNutrientEnum.N, s -> s.getN() > 0 && s.getN() < 250);
//        predicateMap.put(SoilNutrientEnum.P, s -> s.getP() > 0 && s.getP() < 80);
//        predicateMap.put(SoilNutrientEnum.K, s -> s.getK() > 0 && s.getK() < 300);
//        predicateMap.put(SoilNutrientEnum.PH, s -> s.getPH() > 0 && s.getPH() < 14);
//        predicateMap.put(SoilNutrientEnum.OC, s -> s.getOC() > 0 && s.getOC() < 80);
    }

    public static Predicate<SamplePointEntity> get(SoilNutrientEnum soilNutrientEnum){
        return NutrientFilter.predicateMap.get(soilNutrientEnum);
    }
}
