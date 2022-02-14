package com.example.krigingweb.Interpolation.Kriging;

import com.example.krigingweb.Entity.ErrorEntity;
import jsat.regression.Regressor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class InterpolationTask {
    private Regressor regressor;
    private ErrorEntity testErrorEntity;
    private ErrorEntity trainErrorEntity;
    private VariogramPredictor variogramPredictor;

    private Method setNutrientMethod;
}
