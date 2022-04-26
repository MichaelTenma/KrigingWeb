package com.example.krigingweb.Interpolation.Core;

import com.example.krigingweb.Interpolation.Core.Enum.SoilNutrientEnum;

import java.lang.reflect.Method;

public class InterpolationTask{
    public final Regressor regressor;
    public final Method setSoilNutrientMethod;
    public final SoilNutrientEnum soilNutrientEnum;

    public InterpolationTask(Regressor regressor, Method setSoilNutrientMethod, SoilNutrientEnum soilNutrientEnum) {
        this.regressor = regressor;
        this.setSoilNutrientMethod = setSoilNutrientMethod;
        this.soilNutrientEnum = soilNutrientEnum;
    }

//    public void free(){
//        this.regressor.free();
//    }
}
