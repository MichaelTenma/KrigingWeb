package com.example.krigingweb.Interpolation.Core.Kriging;

public abstract class VariogramPredictor {

    protected double range;
    protected double partialSill;
    protected double nugget;

    public abstract double predict(double h);

    public abstract void OLS(int rangeIndex, double range, final double[][] S);
    public double loss(final double[][] S){
        if(range < 0 || partialSill < 0 || nugget < 0){
            return Double.MAX_VALUE;/* 通过巨大的损失惩罚 */
        }

        /* weighted RMSE */
        double weightedRMSE = 0;
        for (double[] doubles : S) {
            double predict_semi = this.predict(doubles[0]);
            double delta_semi = Math.pow(predict_semi - doubles[1], 2);
            weightedRMSE += delta_semi;
//            double weight = (1 - k * 1.0 / rangeIndex);
//            weightedRMSE += 2 * weight * delta_semi / (rangeIndex + 1) / rangeIndex;
        }
        weightedRMSE = Math.sqrt(weightedRMSE / (S.length + 1));
        return weightedRMSE;
    }

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