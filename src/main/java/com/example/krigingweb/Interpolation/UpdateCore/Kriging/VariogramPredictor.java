package com.example.krigingweb.Interpolation.UpdateCore.Kriging;

public abstract class VariogramPredictor {

    protected double range;
    protected double partialSill;
    protected double nugget;

    public abstract double predict(double h);

    protected abstract void OLS(int rangeIndex, final double[][] S);

    public double getRange(){
        return this.range;
    }

    public double getPartialSill(){
        return this.partialSill;
    }

    public double getNugget(){
        return this.nugget;
    }
    public abstract VariogramPredictor clone();
}