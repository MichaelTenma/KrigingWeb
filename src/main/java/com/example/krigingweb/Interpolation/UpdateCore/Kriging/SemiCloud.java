package com.example.krigingweb.Interpolation.UpdateCore.Kriging;

public class SemiCloud<V extends VariogramPredictor> {
    private final double[][] S;

    public SemiCloud(double lag, double[][] u, double[] Z){
        int n = u.length;

        double[] L = new double[(n-1) * n / 2];
        /* 任意设置一个 */
        double minL = distance(u[0][0], u[0][1], u[2][0], u[2][1]);
        double maxL = minL;
        for(int k = 0;k < n - 1;k++){
            for(int j = k + 1;j < n;j++){
                double dis = distance(u[k][0], u[k][1], u[j][0], u[j][1]);
                L[zipIndex(n, k, j)] = dis;
                if(dis < minL) minL = dis;
                if(dis > maxL) maxL = dis;
            }
        }

        int group = (int)Math.ceil((maxL - minL) / lag);

        double[] NArray = new double[group+1];
        NArray[0] = n;
        for(int k = 0;k < n - 1;k++){
            for(int j = k + 1;j < n;j++){
                int i = (int)Math.floor((L[zipIndex(n, k, j)] - minL)/lag) + 1;
                NArray[i] += 1;
            }
        }

        double[] semiArray = new double[group+1];
        for(int k = 0;k < n - 1;k++){
            for(int j = k + 1;j < n;j++){
                int i = (int)Math.floor((L[zipIndex(n, k, j)] - minL)/lag) + 1;
                double h2 = minL + i * lag;
                double h1 = h2 - lag;
                if(L[i] > h1 && L[i] <= h2){
                    semiArray[i] += Math.pow(Z[k] - Z[j], 2);
                }
            }
        }

        this.S = new double[semiArray.length][2];
        for(int i = 0;i < semiArray.length;i++){
            semiArray[i] /= NArray[i];
            this.S[i][0] = minL + (i - 0.5)*lag;
            this.S[i][1] = semiArray[i];
        }
        this.S[0][0] = 0;
        this.S[0][1] = 0;
    }

    private static double distance(double ui_x, double ui_y, double uj_x, double uj_y){
        final double delta_x = ui_x - uj_x;
        final double delta_y = ui_y - uj_y;
        return Math.sqrt(delta_x*delta_x + delta_y*delta_y);
    }

    private static int zipIndex(int n, int k, int j){
        return j - k + k*(2*n-1+k)/2 - 1;
    }

    public V fit(V variogramPredictor){
        V minVariogramPredictor = null;
        double minWeightedRMSE = Double.MAX_VALUE;
        for(int i = 0;i < S.length;i++){
            variogramPredictor.OLS(i, S);
            /* weighted RMSE */
            double weightedRMSE = 0;
            for(int k = 0;k < S.length;k++){
                double predict_semi = variogramPredictor.predict(S[k][0]);
                double delta_semi = Math.pow(predict_semi - S[k][1], 2);
                double weight = 1 - k * 1.0 / S.length;
                weightedRMSE += weight * delta_semi;
            }
            weightedRMSE = Math.sqrt(weightedRMSE / S.length * 2 / (S.length + 1));
            if(weightedRMSE < minWeightedRMSE){
                minWeightedRMSE = weightedRMSE;
                minVariogramPredictor = (V) variogramPredictor.clone();
            }
        }
        return minVariogramPredictor;
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
