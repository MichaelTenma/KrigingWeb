package com.example.krigingweb.Interpolation.Core.Kriging;

public abstract class VariogramPredictor {

    protected double range;
    protected double partialSill;
    protected double nugget;

    public abstract double predict(double h, double range, double partialSill, double nugget);

    protected abstract void OLS(int rangeIndex, double[] distanceArray, double[] semiArray);

    public double getRange(){
        return this.range;
    }

    public double getPartialSill(){
        return this.partialSill;
    }

    public double getNugget(){
        return this.nugget;
    }
}