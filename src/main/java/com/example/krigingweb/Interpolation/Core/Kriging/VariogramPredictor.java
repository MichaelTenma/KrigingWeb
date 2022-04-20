package com.example.krigingweb.Interpolation.Core.Kriging;

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

    public void update(VariogramPredictor variogramPredictor){
        this.range = variogramPredictor.getRange();
        this.partialSill = variogramPredictor.getPartialSill();
        this.nugget = variogramPredictor.getNugget();
    }
}