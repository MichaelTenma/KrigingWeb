package com.example.krigingweb.Interpolation.Core.Kriging;

import java.util.Arrays;

public class SemiCloud<V extends VariogramPredictor> {
    private final int n;
    private final double[][] S;
    private final double[] L;

    public SemiCloud(double lag, double[][] u, double[] Z){
        n = u.length;
        L = new double[(n-1) * n / 2];

        /* 任意设置一个 */
        double minL = MathUtil.distance(u[0][0], u[0][1], u[2][0], u[2][1]);
        double maxL = minL;
        for(int k = 0;k < n - 1;k++){
            for(int j = k + 1;j < n;j++){
                double dis = MathUtil.distance(u[k][0], u[k][1], u[j][0], u[j][1]);
                L[zipIndex(k, j)] = dis;
                if(dis < minL) minL = dis;
                if(dis > maxL) maxL = dis;
            }
        }

        int group = (int)Math.ceil((maxL - minL) / lag);

        double[] NArray = new double[group+1];
        NArray[0] = n;
        for(int k = 0;k < n - 1;k++){
            for(int j = k + 1;j < n;j++){
                int i = (int)Math.floor((L[zipIndex(k, j)] - minL)/lag) + 1;
                NArray[i] += 1;
            }
        }

        double[] semiArray = new double[group+1];
        for(int k = 0;k < n - 1;k++){
            for(int j = k + 1;j < n;j++){
                double curL = L[zipIndex(k, j)];
                int i = (int)Math.floor((curL - minL)/lag) + 1;
                double h2 = minL + i * lag;
                double h1 = h2 - lag;
                if(curL > h1 && curL <= h2){
                    semiArray[i] += Math.pow(Z[k] - Z[j], 2);
                }
            }
        }


        double[][] tmpS = new double[semiArray.length][2];
        for(int i = 0;i < semiArray.length;i++){
            semiArray[i] /= NArray[i];
            tmpS[i][0] = minL + (i - 0.5)*lag;
            tmpS[i][1] = semiArray[i];
        }
        tmpS[0][0] = 0;
        tmpS[0][1] = 0;

        this.S = Arrays.copyOfRange(tmpS, 0, tmpS.length / 2 + 1);
    }

    protected int zipIndex(int k, int j){
        return j - k + k*(2*n - 1 - k)/2 - 1;
    }

    /**
     * @param variogramPredictor 用于拟合的变异模型，会被更新为误差最小的变异模型
     * @return predictSemiMatrix 表示点间预测半变异矩阵，上三角压缩矩阵，不含对角线，
     * 其对角线元素均为拟合得到的变异模型的块金，使用数组的最后一位存储块金，注意处理
     */
    public double[] fit(V variogramPredictor){
        V minVariogramPredictor = variogramPredictor;
        double minWeightedRMSE = Double.MAX_VALUE;
        for(int i = 0;i < S.length;i++){
            variogramPredictor.OLS(i, S);
            /* weighted RMSE */
            double weightedRMSE = 0;
            for(int k = 0;k < S.length;k++){
                double predict_semi = variogramPredictor.predict(S[k][0]);
                double delta_semi = Math.pow(predict_semi - S[k][1], 2);
                double weight = 1 - k * 1.0 / S.length;
//                double weight = 1;
                weightedRMSE += 2 * weight * delta_semi / (S.length + 1) / S.length;
            }
            weightedRMSE = Math.sqrt(weightedRMSE);
            if(weightedRMSE < minWeightedRMSE){
                minWeightedRMSE = weightedRMSE;
                minVariogramPredictor = (V) variogramPredictor.clone();
            }
        }

        double[] predictSemiMatrix = new double[L.length + 1];
        for(int k = 0; k < predictSemiMatrix.length - 1; k++){
            predictSemiMatrix[k] = minVariogramPredictor.predict(L[k]);
        }
        predictSemiMatrix[predictSemiMatrix.length - 1] = minVariogramPredictor.getNugget();

        variogramPredictor.update(minVariogramPredictor);
        return predictSemiMatrix;
    }

    @Override
    public String toString(){
        StringBuilder hString = new StringBuilder("h = [");
        StringBuilder semiString = new StringBuilder("semi = [");
        for (double[] doubles : this.S) {
            hString.append(doubles[0] + ',');
            semiString.append(doubles[1] + ',');
        }
        hString.setCharAt(hString.length()-1,']');
        semiString.setCharAt(semiString.length()-1,']');
        hString.append(";\n");
        semiString.append(";\n");
        return hString.toString() + semiString;
    }
}
