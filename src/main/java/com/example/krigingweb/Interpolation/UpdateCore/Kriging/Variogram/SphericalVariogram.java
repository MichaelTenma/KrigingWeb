package com.example.krigingweb.Interpolation.UpdateCore.Kriging.Variogram;

import com.example.krigingweb.Interpolation.UpdateCore.Kriging.VariogramPredictor;
import com.example.krigingweb.Math.OLSCalculater;
import jsat.linear.DenseMatrix;
import jsat.linear.DenseVector;
import jsat.linear.Vec;
import java.util.Arrays;

public class SphericalVariogram extends VariogramPredictor {

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
    public SphericalVariogram clone() {
        return new SphericalVariogram(this.range, this.partialSill, this.nugget);
    }

    @Override
    public double predict(double h) {
        if (h >= range)
            return nugget + partialSill;
        double p = h / range;
        return nugget + partialSill * 1.5 * p - partialSill * 0.5 * p * p * p;
    }

    @Override
    protected void OLS(int rangeIndex, final double[][] S) {
        int n = rangeIndex + 1;
        double range = S[rangeIndex][0];
        double[][] A = new double[n][2];
        for(int k = 0;k < n;k++){
            final double beta = S[k][0] / range;
            A[k][0] = 1; A[k][1] = beta*(3 - beta*beta);
        }

        // ATA = [[a, b], [c, d]]
        double a = n * 2,b = 0,d = 0;
        for(int i = 0;i < n;i++){
            b += A[i][1];
            d += A[i][1] * A[i][1];
        }
        double c = b;

        double ATs0 = 0, ATs1 = 0;
        for(int i = 0;i < n;i++){
            ATs0 += S[i][1];
            ATs1 += A[i][1] * S[i][1];
        }

        double detATA = a * d - b * c;
        if(detATA == 0) detATA = 1E-9;

        this.nugget = (d * ATs0 - b * ATs1) / detATA;
        this.partialSill = 2 * (a * ATs1 - c * ATs0) / detATA;
        this.range = range;
    }

    @Override
    public String toString() {
        return String.format("range = %f; partialSill = %f; nugget= %f;", this.range, this.partialSill, this.nugget);
    }
}
