package com.example.krigingweb.Interpolation.Core;

public interface Regressor extends MemoryFree{
    double[] predict(double[][] predict_u);
}
