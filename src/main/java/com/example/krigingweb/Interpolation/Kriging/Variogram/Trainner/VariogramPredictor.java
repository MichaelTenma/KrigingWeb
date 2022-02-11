package com.example.krigingweb.Interpolation.Kriging.Variogram.Trainner;

import com.example.krigingweb.MathUtil;

public abstract class VariogramPredictor {

    protected double range;
    protected double partialSill;
    protected double nugget;

    public abstract double predict(double h, double range, double partialSill, double nugget);

    protected abstract void OLS(int rangeIndex, double[] distanceArray, double[] semiArray);

    public double getRange(){
        return this.range;
    }

    public double getPartialSill(){
        return this.partialSill;
    }

    public double getNugget(){
        return this.nugget;
    }

    /**
     * 计算差分最大累积点，视该点为变程点即可
     * 由半变异函数模型易知变程点右侧点值稳定于基台值，围绕基台值不断震荡；
     * 而变程点左侧点值不断增加，或有少许点值减少，但累积值会趋于基台值；
     * 在变程点处累积值将不会再无限制地增加，而是表现为时增时减；
     * 依据上述性质寻找变程点，为避免过早将局部极大值点认为是变程点，需要有
     * @return
     */
//    protected static int calRangeIndex(double[] semiArray){
//        int eachStep = 10;// 每10步检测一次半变异平均值是否有下降，若有下降则缩小步数探测
//        int beginIndex = 0;
//
//        double lastSemiAvg = MathUtil.avg(semiArray);
//        double currentSemiAvg = 0;
//        do {
//            beginIndex += eachStep;
//            currentSemiAvg = MathUtil.avg(beginIndex, semiArray);
//            if (currentSemiAvg < lastSemiAvg + 10) {
//                /* 探测到下降，则从当前区间起始位置缩小步数重新探测 */
//                beginIndex -= eachStep;
//                /* 半变异平均值下降，缩小步数探测 */
//                eachStep /= 2;/* 收敛于0 */
//            }
//
//            lastSemiAvg = currentSemiAvg;
//            /* eachStep > 0
//             * 3 / 2 = 1
//             * 2 / 2 = 1
//             * 1 / 2 = 0
//             */
//        } while (eachStep > 0);
//        return beginIndex;
//    }
}
