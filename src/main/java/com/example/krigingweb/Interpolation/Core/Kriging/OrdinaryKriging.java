package com.example.krigingweb.Interpolation.Core.Kriging;

import com.example.krigingweb.Interpolation.Core.Kriging.Variogram.SphericalVariogram;
import com.example.krigingweb.Interpolation.Core.Util.MathUtil;
import com.example.krigingweb.Interpolation.Core.Regressor;
import org.apache.commons.math3.linear.*;

import java.util.Arrays;

public class OrdinaryKriging implements Regressor {
    private double[][] u;
    private RealMatrix ZT_invM;
    private VariogramPredictor variogramPredictor;
    public OrdinaryKriging(double lag, double maxLag, double[][] u, double[] Z, VariogramPredictor variogramPredictor){
        this.u = u;
        this.variogramPredictor = variogramPredictor;
        int n = u.length;

        SemiCloud<VariogramPredictor> semiCloud = new SemiCloud<>(lag, maxLag, u, Z);
        double[] predictSemiMatrix = semiCloud.fit(variogramPredictor);

//        System.out.println(semiCloud.toString() + variogramPredictor);
        double[][] M = new double[n + 1][n + 1];
        for(int k = 0;k < n - 1;k++){
            for(int j = k + 1;j < n;j++){
                M[k][j] = predictSemiMatrix[semiCloud.zipIndex(k, j)];
                M[j][k] = M[k][j];
            }
        }
        for(int k = 0;k < n;k++){
            /* nugget */
            M[k][k] = predictSemiMatrix[predictSemiMatrix.length - 1];
            M[k][n] = 1;
            M[n][k] = 1;
        }
        M[n][n] = 0;

        RealMatrix invM = null;
        {
            RealMatrix MMatrix = MatrixUtils.createRealMatrix(M);
            EigenDecomposition evd = new EigenDecomposition(MMatrix);
            if(evd.getSolver().isNonSingular()){
                double[] DDiagonal = evd.getRealEigenvalues();
                for(int i = 0;i < DDiagonal.length;i++){
                    DDiagonal[i] = 1.0 / DDiagonal[i];
                }
                DiagonalMatrix invD = new DiagonalMatrix(DDiagonal);
                RealMatrix V = evd.getV();
                RealMatrix VT = evd.getVT();
                invM = V.multiply(invD).multiply(VT);
            }else{
                SingularValueDecomposition svd = new SingularValueDecomposition(MMatrix);
                invM = svd.getSolver().getInverse();
            }
        }

        RealMatrix ZMatrix;
        {
            double[] ZCopy = new double[Z.length + 1];
            System.arraycopy(Z, 0, ZCopy, 0, Z.length);
            ZCopy[ZCopy.length - 1] = 0;
            ZMatrix = MatrixUtils.createRowRealMatrix(ZCopy);
        }
        this.ZT_invM = ZMatrix.multiply(invM);
    }

    public OrdinaryKriging(double lag, double maxLag, double[][] u, double[] Z){
        this(lag, maxLag, u, Z, new SphericalVariogram());
    }

    @Override
    public double[] predict(double[][] predict_u){
        int n = this.u.length;
        int m = predict_u.length;
        double[][] P = new double[n + 1][m];

        for(int k = 0;k < n;k++){
            double[] uEle = this.u[k];
            for(int j = 0;j < m;j++){
                double[] predict_uEle = predict_u[j];
                double predict_semi = this.variogramPredictor.predict(MathUtil.distance(
                    uEle[0], uEle[1], predict_uEle[0], predict_uEle[1]
                ));
                P[k][j] = predict_semi;
            }
        }
        for(int i = 0;i < m;i++) P[n][i] = 1;
        RealMatrix PMatrix = MatrixUtils.createRealMatrix(P);
        RealMatrix realMatrix = this.ZT_invM.multiply(PMatrix);
//        boolean isNeg = Arrays.stream(realMatrix.getRow(0)).anyMatch(e -> e <= 0);
//        if(isNeg){
//            isNeg = true;
//        }

        return realMatrix.getRow(0);
    }
}