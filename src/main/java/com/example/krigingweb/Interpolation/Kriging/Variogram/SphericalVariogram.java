package com.example.krigingweb.Interpolation.Kriging.Variogram;

import com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner.OLSCalculater;
import com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner.VariogramPredictor;
import jsat.linear.DenseMatrix;
import jsat.linear.DenseVector;
import jsat.linear.Vec;
import jsat.regression.OrdinaryKriging;
import jsat.regression.RegressionDataSet;

public class SphericalVariogram implements OrdinaryKriging.Variogram, VariogramPredictor {
    private double range;
    private double partialSill;
    private double nugget;

    public SphericalVariogram(){
        this(0.1, 0.1, 0);
    }

    public SphericalVariogram(double range, double partialSill){
        this(range, partialSill, 0);
    }

    public SphericalVariogram(double range, double partialSill, double nugget) {
        if (range <= 0)
            throw new IllegalArgumentException("Invalid parameter range = " + range);
        if (partialSill <= 0)
            throw new IllegalArgumentException("Invalid parameter partialSill = " + partialSill);
        if (nugget < 0)
            throw new IllegalArgumentException("Invalid parameter nugget = " + nugget);

        this.range = range;
        this.partialSill = partialSill;
        this.nugget = nugget;
    }

    @Override
    public void train(RegressionDataSet dataSet, double nugget) {
        this.nugget = nugget;
    }

    @Override
    public double val(double h) {
        return this.predict(h, this.range, this.partialSill, this.nugget);
    }

    @Override
    public SphericalVariogram clone() {
        return new SphericalVariogram(this.range, this.partialSill, this.nugget);
    }

    @Override
    public double predict(double h, double range, double partialSill, double nugget) {
        if (h >= range)
            return nugget + partialSill;
        double p = h / range;
        return nugget + partialSill * 1.5 * p - partialSill * 0.5 * p * p * p;
    }

    @Override
    public void OLS(double[] distanceArray, double[] semiArray) {
        Vec semiVec = DenseVector.toDenseVec(semiArray);
        DenseMatrix A = new DenseMatrix(semiArray.length, 3);
        for(int i = 0; i < distanceArray.length;i++){
            double h = distanceArray[i];
            A.updateRow(i, 1, DenseVector.toDenseVec(-0.5*h*h*h, 1.5*h, 1));
        }

        Vec X = OLSCalculater.OLS(A, semiVec);
        double n = X.get(0);
        double m = X.get(1);
        this.nugget = X.get(2);
        this.range = Math.sqrt(m / n);
        this.partialSill = m * this.range;
        System.out.println(String.format("OLS: range = %f; partialSill = %f; nugget= %f;", this.range, this.partialSill, this.nugget));
    }

    @Override
    public double getRange() {
        return this.range;
    }

    @Override
    public double getPartialSill() {
        return this.partialSill;
    }

    @Override
    public double getNugget() {
        return this.nugget;
    }
}
