//package com.example.krigingweb.Interpolation.Kriging.Variogram;
//
//import com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner.OLSCalculater;
//import com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner.VariogramPredictor;
//import jsat.linear.DenseMatrix;
//import jsat.linear.DenseVector;
//import jsat.linear.Vec;
//import jsat.regression.OrdinaryKriging;
//import jsat.regression.RegressionDataSet;
//
//public class Log10Variogram implements OrdinaryKriging.Variogram, VariogramPredictor {
//    private double range;
//    private double b;
//    private double c;
//
//    public Log10Variogram(){
//        this(1, 1);
//    }
//    public Log10Variogram(double b, double c) {
//        this.b = b;
//        this.c = c;
//    }
//
//    @Override
//    public double predict(double h, double range, double partialSill, double nugget) {
//        return this.b * Math.log10(h + 1) + this.c;
//    }
//
//    @Override
//    public void OLS(double[] distanceArray, double[] semiArray) {
//        double predictSill = 0;
//        for(double kk : semiArray){
//            predictSill += kk;
//        }
//        predictSill /= semiArray.length;
////        System.out.println("predictSill: " + predictSill);
//
//        Vec semiVec = DenseVector.toDenseVec(semiArray);
//        DenseMatrix A = new DenseMatrix(semiArray.length, 2);
//        for(int i = 0; i < distanceArray.length;i++){
//            double h = distanceArray[i];
//            A.updateRow(i, 1, DenseVector.toDenseVec(Math.log(h+1), 1));
//        }
//        /**
//         * 对数模型的最小二乘解
//         *
//         * AX = Y
//         * A'AX = A'Y
//         * 最小二乘解：X = inv(A'A)A'Y
//         */
//
//        Vec X = OLSCalculater.OLS(A, semiVec);
//        this.b = X.get(0);
//        this.c = X.get(1);
//
//        this.range = -1 + Math.pow(10, (predictSill - this.c) / this.b);
//        System.out.println(String.format("range = %f; b = %f, c= %f;", range, this.b, this.c));
//    }
//
//    @Override
//    public double getRange() {
//        return this.range;
//    }
//
//    @Override
//    public double getPartialSill() {
//        return this.val(this.getRange()) - this.getNugget();
//    }
//
//    @Override
//    public double getNugget() {
//        return this.val(0);
//    }
//
//    @Override
//    public void train(RegressionDataSet dataSet, double nugget) {
//    }
//
//    @Override
//    public double val(double r) {
//        return this.predict(r, this.getRange(), this.getPartialSill(), this.getNugget());
//    }
//
//    @Override
//    public OrdinaryKriging.Variogram clone() {
//        return new Log10Variogram(this.b, this.c);
//    }
//}
