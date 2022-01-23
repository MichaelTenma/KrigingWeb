package com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner;

public interface VariogramPredictor {
//    public static double predict(double h, double range, double partialSill, double nugget){
//        return 0;
//    }

    double predict(double h, double range, double partialSill, double nugget);

    void OLS(double[] distanceArray, double[] semiArray);

    double getRange();
    double getPartialSill();
    double getNugget();
}