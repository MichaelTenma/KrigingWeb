package com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner;

import jsat.classifiers.DataPoint;
import jsat.classifiers.DataPointPair;
import jsat.linear.*;
import jsat.regression.RegressionDataSet;

import java.util.*;

public class SemiCloud {
    private final RegressionDataSet originalDataSet;
    private double[] semiArray;
    private double[] distanceArray;

    private double initRange;
    private final double lag;
    private final double lagDistance;/* 滞后距离 */

    private final VariogramPredictor variogramPredictor;
    public SemiCloud(RegressionDataSet originalDataSet, double lag, double lagDistance, VariogramPredictor variogramPredictor) {
        this.originalDataSet = originalDataSet;
        this.lag = lag;
        this.lagDistance = lagDistance;
        this.variogramPredictor = variogramPredictor;

        this.semiVariogram();
        this.initRange = this.calInitRange();
    }

    private void semiVariogram(){
        Map<Double, List<Double>> map = new HashMap<>();

        List<DataPointPair<Double>> list = this.originalDataSet.getAsDPPList();

        for(int y = 0;y < list.size();y++){
            DataPointPair<Double> A = list.get(y);
            DataPoint dataPointA = A.getDataPoint();
            double pairA = A.getPair();
            for(int x = y + 1;x < list.size();x++){
                DataPointPair<Double> B = list.get(x);
                DataPoint dataPointB = B.getDataPoint();
                double pairB = B.getPair();

                double distance = dataPointA.getNumericalValues().pNormDist(2, dataPointB.getNumericalValues());
                double diff = SemiCloud.squaredDifferences(pairA, pairB);

                List<Double> entry = map.computeIfAbsent(distance, k -> new ArrayList<>());
                entry.add(diff);
            }
        }

        this.distanceArray = new double[ map.size() ];
        this.semiArray = new double[ this.distanceArray.length ];
        int index = 0;
        for(Map.Entry<Double, List<Double>> entry : map.entrySet()){
            double distance = entry.getKey();
            List<Double> diffList = entry.getValue();
            int N = diffList.size();

            double diff = 0;
            for(double tempDiff : diffList){
                diff += tempDiff;
            }

            double semi = diff / 2 / N;

            this.distanceArray[index] = distance;
            this.semiArray[index] = semi;
            index++;
        }


        GrouperResult grouperResult = Grouper.groupDistance(this.distanceArray, this.semiArray, this.lag, this.lagDistance);
        this.distanceArray = grouperResult.distanceArray;
        this.semiArray = grouperResult.semiArray;
    }

    private double calInitRange(){
        double predictSill = 0;
        for(double kk : this.semiArray){
            predictSill += kk;
        }
        predictSill /= this.semiArray.length;
        System.out.println("predictSill: " + predictSill);

        DenseMatrix A = new DenseMatrix(this.semiArray.length, 2);
        DenseVector semiVec = new DenseVector(this.semiArray.length);
        for(int i = 0; i < this.distanceArray.length;i++){
            double h = this.distanceArray[i];
            double semi = this.semiArray[i];

            A.updateRow(i, 1, DenseVector.toDenseVec(Math.log(h+1), 1));
            semiVec.set(i, semi);
        }

        {
            List<Double> testList = new ArrayList<>(this.distanceArray.length);
            for(double dis : this.distanceArray){
                testList.add(dis);
            }

            String xxxx = testList.toString();

            testList.clear();
            for(double dis : this.semiArray){
                testList.add(dis);
            }
            String yyyy = testList.toString();
            System.out.println();
        }

        /**
         * 对数模型的最小二乘解有问题
         *
         * AX = Y
         * A'AX = A'Y
         * 最小二乘解：X = inv(A'A)A'Y
         */
        DenseMatrix AT = A.transpose();
        Matrix ATA = AT.multiply(A);
        Vec ATY = AT.multiply(semiVec);
        LUPDecomposition lup = new LUPDecomposition(ATA);

        Vec X = lup.solve(ATY);
        if(Double.isNaN(lup.det()) || Math.abs(lup.det()) < 1e-5) {
            SingularValueDecomposition svd = new SingularValueDecomposition(ATA);
            X = svd.solve(ATY);
        }

        double b = X.get(0);
        double c = X.get(1);

        double range = -1 + Math.pow(2, (predictSill - c) / b);
        System.out.println(String.format("range = %f; b = %f, c= %f;", range, b, c));
        return range;
    }

    public double getInitRange() {
        return initRange;
    }

    public double loss(double range, double partialSill, double nugget){
        if(range < 0 || partialSill < 0 || nugget < 0){
            return 999999999;/* 通过巨大的损失惩罚 */
        }
        double g = 0;
        for(int i = 0;i < this.distanceArray.length;i++){
            double distance = this.distanceArray[i];
            double realSemi = this.semiArray[i];
            double predictSemi = this.variogramPredictor.predict(distance, range, partialSill, nugget);

//            double factor = distance < range ? 10000 : 0.1;
            g += Math.pow(predictSemi - realSemi, 2);
        }
//        System.out.println(g);
        return Math.sqrt(g / this.distanceArray.length);
    }

    /**
     * 计算两点之间的指标值平方差异
     * @param Zi i点的指标值
     * @param Zj j点的指标值
     * @return 平方差异
     */
    private static double squaredDifferences(double Zi, double Zj){
        return Math.pow((Zi - Zj), 2);
    }

    /**
     * 计算点a与点b之间的欧几里得距离（二范数）
     * @param aX 点a的X坐标
     * @param aY 点a的Y坐标
     * @param bX 点b的X坐标
     * @param bY 点b的Y坐标
     * @return 欧几里得距离
     */
    private static double norm2(double aX, double aY, double bX, double bY){
        return Math.sqrt(Math.pow((aX - bX), 2) + Math.pow((aY - bY), 2));
    }
}
