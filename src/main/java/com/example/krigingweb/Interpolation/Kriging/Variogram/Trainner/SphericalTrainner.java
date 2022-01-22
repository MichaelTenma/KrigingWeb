package com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner;

import jsat.classifiers.DataPoint;
import jsat.classifiers.DataPointPair;
import jsat.linear.Vec;
import jsat.regression.RegressionDataSet;
import jsat.utils.Tuple3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SphericalTrainner {
    private final RegressionDataSet originalDataSet;
    private double[] semiArray;
    private double[] distanceArray;

    public SphericalTrainner(RegressionDataSet originalDataSet){
        this.originalDataSet = originalDataSet;
        this.semiVariogram();
    }

    private void semiVariogram(){
        Map<Double, List<Double>> map = new HashMap<>();

        List<DataPointPair<Double>> list = this.originalDataSet.getAsDPPList();
        for(DataPointPair<Double> A : list){
            DataPoint dataPointA = A.getDataPoint();
            double pairA = A.getPair();
            for(DataPointPair<Double> B : list){
                if(!A.equals(B)){
                    DataPoint dataPointB = A.getDataPoint();
                    double pairB = B.getPair();

                    double distance = dataPointA.getNumericalValues().pNormDist(2, dataPointB.getNumericalValues());
                    double diff = SphericalTrainner.squaredDifferences(pairA, pairB);

                    List<Double> entry = map.computeIfAbsent(distance, k -> new ArrayList<>());
                    entry.add(diff);
                }
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
    }

    private static double spherical(double h, double range, double partialSill, double nugget){
        if (h >= range)
            return nugget + partialSill;
        double p = h / range;
        return nugget + partialSill * (1.5 * p - 0.5 * p * p * p);
    }

    public double loss(double range, double partialSill, double nugget){
        if(range < 0 || partialSill < 0 || nugget < 0){
            return 999999999;/* 通过巨大的损失惩罚 */
        }
        double g = 0;
        for(int i = 0;i < this.distanceArray.length;i++){
            double distance = this.distanceArray[i];
            double realSemi = this.semiArray[i];
            double predictSemi = SphericalTrainner.spherical(distance, range, partialSill, nugget);
            g += Math.pow(predictSemi - realSemi, 2);
        }
        System.out.println(g);
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
