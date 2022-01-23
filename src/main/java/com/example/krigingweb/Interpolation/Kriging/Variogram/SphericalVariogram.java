package com.example.krigingweb.Interpolation.Kriging.Variogram;

import com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner.VariogramPredictor;
import jsat.regression.OrdinaryKriging;
import jsat.regression.RegressionDataSet;

public class SphericalVariogram implements OrdinaryKriging.Variogram, VariogramPredictor {
    private final double range;
    private final double partialSill;
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
}
