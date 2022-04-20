package com.example.krigingweb.Interpolation.Core;

import java.lang.reflect.Method;

public class InterpolationTask {
    public final Regressor regressor;
    public final Method setSoilNutrientMethod;

    public InterpolationTask(Regressor regressor, Method setSoilNutrientMethod) {
        this.regressor = regressor;
        this.setSoilNutrientMethod = setSoilNutrientMethod;
    }
}
