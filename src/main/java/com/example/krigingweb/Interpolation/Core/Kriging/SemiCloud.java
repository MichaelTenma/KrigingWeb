package com.example.krigingweb.Interpolation.Core.Kriging;

import java.util.Arrays;

public class SemiCloud<V extends VariogramPredictor> {
    private final int n;
    private final double[][] S;
    private final double[] L;

    public SemiCloud(double lag, double maxLag, double[][] u, double[] Z){
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

        {
            int index = 0;
            double[][] tmpS = new double[semiArray.length][2];
            for(int i = 0;i < semiArray.length;i++){
                if(NArray[i] != 0) semiArray[i] /= NArray[i];
                tmpS[i][0] = minL + (i - 0.5)*lag;
                tmpS[i][1] = semiArray[i];

                if(tmpS[i][0] <= maxLag){
                    index = i;
                }
            }
            tmpS[0][0] = 0;
            tmpS[0][1] = 0;

//            index = Math.min(index, tmpS.length / 2) + 1;
            index ++;
            this.S = Arrays.copyOfRange(tmpS, 1, index);
        }
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
        /*
         * 1. 将研究区域分成15组，[left, right]
         * 2. 以每组的中心作为变程点拟合变异模型
         * 3. 计算每组的误差，并挑选出误差最小的组
         * 4. 取与该组相邻的组，即为2~3组，视作新的研究范围
         * 5. 若新研究范围内有超过一个可选变程点存在，则重复第1步
         * 6. 该可选变程点即为最优变程点
         */
        V minVariogramPredictor = (V) variogramPredictor.clone();
        double minWeightedRMSE = variogramPredictor.loss(S);
        double left = S[0][0], right = S[S.length - 1][0];
        int groupNumber = 15;
        while(true){
            final double gap = (right - left) / groupNumber;
            double minRange = 0;
            for(double cur = left + gap / 2;cur < right;cur += gap){
                int rangeIndex = S.length - 1;
                for(int i = 0; i < S.length - 1;i++){
                    if(cur >= S[i][0] && cur <= S[i+1][0]){
                        rangeIndex = i;
                        break;
                    }
                }
                variogramPredictor.OLS(rangeIndex, cur, S);

                /* weighted RMSE */
                double weightedRMSE = variogramPredictor.loss(Arrays.copyOfRange(S, 0, rangeIndex + 1));
                if(weightedRMSE < minWeightedRMSE){
                    minWeightedRMSE = weightedRMSE;
                    minVariogramPredictor.update(variogramPredictor);
                    minRange = cur;
                }
            }

            left = Math.max(minRange - gap, left);
            right = Math.min(minRange + gap, right);
            if(left >= right) break;

            int sum = 0;
            for (double[] doubles : S) {
                if(doubles[0] < left) continue;
                if(doubles[0] <= right) sum++;
                else break;
            }
            if(sum <= 1) break;
        }

//        System.out.println(this.toString() + minVariogramPredictor);
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
        StringBuilder distanceString = new StringBuilder("distance = [");
        StringBuilder semiString = new StringBuilder("semi = [");
        for (double[] doubles : this.S) {
            distanceString.append(doubles[0]).append(',');
            semiString.append(doubles[1]).append(',');
        }
        distanceString.setCharAt(distanceString.length()-1,']');
        semiString.setCharAt(semiString.length()-1,']');
        distanceString.append(";\n");
        semiString.append(";\n");
        return distanceString.toString() + semiString;
    }
}
