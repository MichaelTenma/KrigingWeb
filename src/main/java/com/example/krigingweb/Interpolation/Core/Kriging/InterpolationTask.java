package com.example.krigingweb.Interpolation.Core.Kriging;

import com.example.krigingweb.Interpolation.Core.ErrorEntity;
import jsat.linear.Vec;
import jsat.regression.Regressor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class InterpolationTask {
    private Regressor regressor;
    private Vec originalPoint;
    private VariogramPredictor variogramPredictor;
    private Method setNutrientMethod;
    private ErrorEntity trainErrorEntity;
    private ErrorEntity testErrorEntity;
}
