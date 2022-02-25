package com.example.krigingweb.Interpolation.Core.Kriging.Variogram;

import com.example.krigingweb.Interpolation.Core.Kriging.VariogramPredictor;
import com.example.krigingweb.Math.OLSCalculater;
import jsat.linear.DenseMatrix;
import jsat.linear.DenseVector;
import jsat.linear.Vec;
import jsat.regression.OrdinaryKriging;
import jsat.regression.RegressionDataSet;

import java.util.Arrays;

public class SphericalVariogram extends VariogramPredictor implements OrdinaryKriging.Variogram {

    public SphericalVariogram(){
        this(0.1, 0.1, 0);
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
    protected void OLS(int rangeIndex, double[] distanceArray, double[] semiArray) {
        this.range = distanceArray[rangeIndex];

        /* 只拟合变程点左侧 */
        distanceArray = Arrays.copyOfRange(distanceArray, 0, rangeIndex);
        semiArray = Arrays.copyOfRange(semiArray, 0, rangeIndex);

        Vec semiVec = DenseVector.toDenseVec(semiArray);
        DenseMatrix A = new DenseMatrix(semiArray.length, 2);
        for(int i = 0; i < distanceArray.length;i++){
            double h = distanceArray[i];
            double p = h / this.range;
            double tmp = (1.5 * p) - (0.5 * p * p * p);
            A.updateRow(i, 1, DenseVector.toDenseVec(tmp, 1));
        }

        Vec X = OLSCalculater.OLS(A, semiVec);
        this.partialSill = X.get(0);
        this.nugget = X.get(1);
    }

    @Override
    public String toString() {
        return String.format("range = %f; partialSill = %f; nugget= %f;", this.range, this.partialSill, this.nugget);
    }
}
