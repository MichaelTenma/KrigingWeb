package com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner;

@FunctionalInterface
public interface VariogramPredictor {
//    public static double predict(double h, double range, double partialSill, double nugget){
//        return 0;
//    }

    double predict(double h, double range, double partialSill, double nugget);
}