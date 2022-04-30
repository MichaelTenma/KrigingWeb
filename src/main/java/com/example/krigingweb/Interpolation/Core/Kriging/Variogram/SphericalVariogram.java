package com.example.krigingweb.Interpolation.Core.Kriging.Variogram;

import com.example.krigingweb.Interpolation.Core.Kriging.VariogramPredictor;
import java.util.ArrayList;
import java.util.List;

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
    public void OLS(int rangeIndex, double range, final double[][] S) {
        int n = S.length;
        /* KKT */
        double[] beta = new double[rangeIndex + 1];

        double k1 = 0, k2 = 0, k3 = 0, k4 = 0, k5 = 0, k6 = 0, k7 = 0, k8 = 0, k9 = 0, k10 = 0;
        for(int i = 0;i <= rangeIndex;i++){
            beta[i] = S[i][0] / range;
            final double tmpBetaMinus = 1.5*beta[i] - 0.5*beta[i]*beta[i]*beta[i];
            k1 += S[i][1] * tmpBetaMinus;
            k2 += tmpBetaMinus;
            k3 += tmpBetaMinus * tmpBetaMinus;
            k9 += tmpBetaMinus;
            k7 += S[i][1];
        }
        for(int i = rangeIndex + 1;i < S.length;i++){
            k4 += S[i][1];
            k7 += S[i][1];
        }
        k1 = -2*k1;
        k2 = 2*k2;
        k3 = 2*k3;
        k4 = -2*k4;
        k5 = 2*(n - rangeIndex - 1);
        k6 = k5;
        k7 = -2*k7;
        k8 = 2*n;
        k9 = 2 * k9;
        k10 = k5;

        List<double[]> resList = new ArrayList<>(11);
        {/* b != 0, c != 0, λ1 = 0, λ2 = 0 */
            double det = k8 * (k3 + k6) - (k2 + k5) * (k9 + k10);
            if(det != 0){
                double b = (-k8*(k1+k4)+k7*(k2+k5))/det;
                double c = ((k9+k10)*(k1+k4)-k7*(k3+k6))/det;
                if(b > 0 && c > 0){
                    resList.add(new double[]{b, c});
                }else{
                    /* 舍去 */
                }
            }else{
                /* 如何求奇异矩阵的伪逆 */
                System.out.println("需要求伪逆");
            }
        }

        {/* b != 0, c = 0, λ1 = 0, λ2 = 0 */
            double c = 0;/* 必可逆 */
            double b = (-(k3+k6)*(k1+k4)-k7*(k9+k10))/((k3+k6)*(k3+k6)+(k9+k10)*(k9+k10));
            if(b > 0){
                resList.add(new double[]{b, c});
            }else{
                /* 舍去 */
            }
        }

        {/* b != 0, c = 0, λ1 = 0, λ2 != 0 */
            double det = -(k3+k6);/* 必可逆，k3+k6 != 0 */
            double c = 0;
            double b = (k1 + k4)/det;/* 必大于0 */
            double lambda2 = ((k1+k4)*(k9+k10)-k7*(k3+k6))/det;
            if(lambda2 > 0){
                resList.add(new double[]{b, c});
            }else{
                /* 舍去 */
            }
        }

        {/* b = 0, c != 0, λ1 = 0, λ2 = 0 */
            double b = 0;/* 必可逆 */
            double c = (-(k1+k4)*(k2+k5)-k7*k8)/((k2+k5)*(k2+k5)+k8*k8);/* c > 0 */
            resList.add(new double[]{b, c});
        }

        {/* b = 0, c != 0, λ1 != 0, λ2 = 0 */
            double b = 0;/* 必可逆 */
            double det = -k8;
            double c = -k7 / det;/* c < 0 */
            double lambda1 = (k8*(k1+k4)-k7*(k2+k5))/det;
            if(lambda1 > 0){
                if(c > 0){
                    resList.add(new double[]{b, c});
                }else{
                    /* 舍去 */
                }
            }else{
                /* 舍去 */
            }
        }

        {/* b = 0, c = 0, λ1 = 0, λ2 = 0 */
            if((k1 + k4) == k7){
                double b = 0, c = 0;
                resList.add(new double[]{b, c});
            }else{
                /* 无解 */
            }
        }

        {/* b = 0, c = 0, λ1 != 0, λ2 = 0 */
            double lambda1 = k1 + k4;
            if(k7 == 0 && lambda1 > 0){
                double b = 0, c = 0;
                resList.add(new double[]{b, c});
            }else{
                /* 无解，或λ1 <= 0 */
            }
        }

        {
            /* b = 0, c = 0, λ1 = 0, λ2 != 0 */
            /* b = 0, c = 0, λ1 != 0, λ2 != 0 */
            /* 其解均不满足广义拉格朗日乘子大于等于0的条件，舍去 */
        }

        double minLoss = Double.MAX_VALUE;
        double min_b = 0;
        double min_c = 0;
        for(double[] doubles : resList){
            double b = doubles[0];
            double c = doubles[1];

            double loss = 0;
            for(int i = 0;i <= rangeIndex;i++){
                double tmp = S[i][1] - c - b * (1.5 * beta[i] - 0.5 * beta[i] * beta[i] * beta[i]);
                loss += tmp * tmp;
            }

            for(int i = rangeIndex + 1;i < S.length;i++){
                double tmp = S[i][1] - c - b;
                loss += tmp * tmp;
            }
            if(loss <= minLoss){
                min_b = b;
                min_c = c;
                minLoss = loss;
            }
        }

        this.range = range;
        this.nugget = min_c;
        this.partialSill = min_b;
    }

    @Override
    public String toString() {
        return String.format("range = %f; partialSill = %f; nugget= %f;", this.range, this.partialSill, this.nugget);
    }
}
