package com.example.krigingweb.Interpolation.Core.Kriging;

import com.example.krigingweb.Math.MathUtil;
import jsat.classifiers.DataPoint;
import jsat.classifiers.DataPointPair;
import jsat.regression.RegressionDataSet;

import java.util.*;

public class SemiCloud<V extends VariogramPredictor> {
    private final RegressionDataSet originalDataSet;
    private double[] semiArray;
    private double[] distanceArray;

    private final double lag;
    private final double lagDistance;/* 滞后距离 */

    private final V variogramPredictor;
    public SemiCloud(RegressionDataSet originalDataSet, double lag, double lagDistance, V variogramPredictor) {
        this.originalDataSet = originalDataSet;
        this.lag = lag;
        this.lagDistance = lagDistance;
        this.variogramPredictor = variogramPredictor;

        this.semiVariogram();
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

        MathUtil.arrayToString("distance", this.distanceArray);
        MathUtil.arrayToString("semi", this.semiArray);
    }

    public V trainVariogram(){
        int rangeIndex = this.calRangeIndex(this.distanceArray, this.semiArray);
        this.variogramPredictor.OLS(rangeIndex, this.distanceArray, this.semiArray);
        return this.variogramPredictor;
    }

    private int calRangeIndex(double[] distanceArray, double[] semiArray){
        int beginRangeIndex = 0;
        int endRangeIndex = distanceArray.length - 1;

        int groupNum = 15;
        while((endRangeIndex - beginRangeIndex) > 2){/* 若间隔为1，则说明左右端点相邻 */
            final int length = (endRangeIndex - beginRangeIndex) + 1;
            int groupStep = (int) Math.ceil(length * 1.0 / groupNum);/* 向上取整 */
            if(groupStep < 1){/* 每组间隔至少为1步 */
                groupStep = 1;
                groupNum = length;
            }

            /* 计算每一组的RMSE，找出最小RMSE的变程点 */
            double minRMSE = 0.0;
            boolean firstFlag = true;

            /* 用于记录最小RMSE变程点所在区间 */
            int nextBeginRangeIndex = beginRangeIndex - groupStep;
            int nextEndRangeIndex = beginRangeIndex + 2 * groupStep;

            /* 用每一组中点RMSE作为该组的平均值 */
            for(int leftRangeIndex = beginRangeIndex;leftRangeIndex < endRangeIndex;){
                /* 当前组中点的距离 */
                int rightRangeIndex = leftRangeIndex + groupStep;
                /* 不能超越endRangeIndex */
                if(rightRangeIndex > endRangeIndex) rightRangeIndex = endRangeIndex;

                final int middleRangeIndex = (leftRangeIndex + rightRangeIndex) / 2;
                this.variogramPredictor.OLS(middleRangeIndex, distanceArray, semiArray);

                /* 变程点左侧RMSE */
                double RMSE = this.loss(this.variogramPredictor);

                if(firstFlag){
                    minRMSE = RMSE;
                    firstFlag = false;
                }else if(RMSE < minRMSE){
                    /* 找出最小RMSE的变程点 */
                    minRMSE = RMSE;

                    /* 最优变程点在minRMSE所在的区间，取相邻的区间，一共保留三个区间 */
                    nextBeginRangeIndex = middleRangeIndex - groupStep;
                    nextEndRangeIndex = middleRangeIndex + groupStep;
                }
                leftRangeIndex = rightRangeIndex;
            }

            /* 更新探测区间 */
            beginRangeIndex = nextBeginRangeIndex;
            endRangeIndex = nextEndRangeIndex;

            /* 避免越界 */
            if(beginRangeIndex < 0) beginRangeIndex = 0;
            if(endRangeIndex >= distanceArray.length) endRangeIndex = distanceArray.length - 1;

        }
        return (beginRangeIndex + endRangeIndex) / 2;
    }

    public double loss(V variogramPredictor){
        return this.loss(
            variogramPredictor.getRange(),
            variogramPredictor.getPartialSill(),
            variogramPredictor.getNugget()
        );
    }

    public double loss(double range, double partialSill, double nugget){
        return SemiCloud.loss(range, partialSill, nugget, this.distanceArray, this.semiArray, this.variogramPredictor);
    }

    private static double loss(
        double range, double partialSill, double nugget,
        double[] distanceArray, double[] semiArray,
        VariogramPredictor variogramPredictor
    ){
        if(range < 0 || partialSill < 0 || nugget < 0){
            return 999999999;/* 通过巨大的损失惩罚 */
        }

        double g = 0;
        for(int i = 0;i < distanceArray.length;i++){
            double distance = distanceArray[i];
            double realSemi = semiArray[i];
            double predictSemi = variogramPredictor.predict(distance, range, partialSill, nugget);

            g += Math.pow(predictSemi - realSemi, 2);
        }
        return Math.sqrt(g / distanceArray.length);
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

}
