package com.example.krigingweb.Interpolation.Core.Kriging;

import com.example.krigingweb.Interpolation.Core.Kriging.Variogram.SphericalVariogram;
import com.example.krigingweb.Interpolation.Core.Regressor;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

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

        RealMatrix MMatrix = MatrixUtils.createRealMatrix(M);
//        DoubleMatrix2D MMatrix = DoubleFactory2D.dense.make(M);
        EigenDecomposition evd = new EigenDecomposition(MMatrix);
//        EigenvalueDecomposition evd = new EigenvalueDecomposition(MMatrix);

        double[] DDiagonal = evd.getRealEigenvalues();
        for(int i = 0;i < DDiagonal.length;i++){
            DDiagonal[i] = 1.0 / DDiagonal[i];
        }
//        DiagonalMatrix DDiagonal = new DiagonalMatrix(evd.getRealEigenvalues());
        DiagonalMatrix invD = new DiagonalMatrix(DDiagonal);
//        DoubleMatrix1D DDiagonalInverse = evd.getRealEigenvalues().assign(v -> 1.0 / v);
//        DoubleMatrix2D invD = DoubleFactory2D.dense.diagonal(DDiagonalInverse);

        RealMatrix V = evd.getV();
        RealMatrix VT = evd.getVT();
//        DoubleMatrix2D V = evd.getV();
//        DoubleMatrix2D VT = Algebra.DEFAULT.transpose(V);

        RealMatrix invM = V.multiply(invD).multiply(VT);
//        DoubleMatrix2D invM = Algebra.DEFAULT.mult(Algebra.DEFAULT.mult(V, invD), VT);
        RealMatrix ZMatrix;
        {
            double[] ZCopy = new double[Z.length + 1];
            for(int i = 0;i < Z.length;i++){
                ZCopy[i] = Z[i];
            }
            ZCopy[ZCopy.length - 1] = 0;
//            ZMatrix = DoubleFactory2D.dense.make(ZCopy);
            ZMatrix = MatrixUtils.createRowRealMatrix(ZCopy);
        }
//        this.ZT_invM = Algebra.DEFAULT.mult(ZMatrix,invM);
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
        for (double[] uEle : this.u) {
            for (double[] predict_uEle : predict_u) {
                this.variogramPredictor.predict(MathUtil.distance(
                    uEle[0], uEle[1], predict_uEle[0], predict_uEle[1]
                ));
            }
        }
        for(int i = 0;i < m;i++) P[n][i] = 1;
        RealMatrix PMatrix = MatrixUtils.createRealMatrix(P);
//        DoubleMatrix2D PMatrix = DoubleFactory2D.dense.make(P);
        RealMatrix realMatrix = this.ZT_invM.multiply(PMatrix);
//        DoubleMatrix2D resMatrix = Algebra.DEFAULT.mult(this.ZT_invM, PMatrix);
//        return resMatrix.toArray()[0];
        return realMatrix.getRow(0);
    }

//    @Override
//    public void free() {
//        this.u = null;
//        this.ZT_invM = null;
//        this.variogramPredictor = null;
//    }
}