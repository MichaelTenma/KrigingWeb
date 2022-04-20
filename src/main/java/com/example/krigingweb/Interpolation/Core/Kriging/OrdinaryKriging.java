package com.example.krigingweb.Interpolation.Core.Kriging;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import com.example.krigingweb.Interpolation.Core.Kriging.Variogram.SphericalVariogram;
import com.example.krigingweb.Interpolation.Core.Regressor;

public class OrdinaryKriging implements Regressor {
    private final double[][] u;

    private final DoubleMatrix2D ZT_invM;
    private final VariogramPredictor variogramPredictor;
    public OrdinaryKriging(double lag, double[][] u, double[] Z, VariogramPredictor variogramPredictor){
        this.u = u;
        this.variogramPredictor = variogramPredictor;
        int n = u.length;

        SemiCloud<VariogramPredictor> semiCloud = new SemiCloud<>(lag, u, Z);
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

        /**
         * Exception in thread "interpolaterThread1" java.lang.ArrayIndexOutOfBoundsException: 373
         * 	at cern.colt.matrix.linalg.EigenvalueDecomposition.tql2(Unknown Source)
         * 	at cern.colt.matrix.linalg.EigenvalueDecomposition.<init>(Unknown Source)
         */
        EigenvalueDecomposition evd = new EigenvalueDecomposition(DoubleFactory2D.dense.make(M));
        DoubleMatrix1D DDiagonalInverse = evd.getRealEigenvalues().assign(v -> 1.0 / v);
        DoubleMatrix2D invD = DoubleFactory2D.dense.diagonal(DDiagonalInverse);
        DoubleMatrix2D V = evd.getV();
        DoubleMatrix2D VT = Algebra.DEFAULT.transpose(V);

        DoubleMatrix2D invM = Algebra.DEFAULT.mult(Algebra.DEFAULT.mult(V, invD), VT);
        DoubleMatrix2D ZMatrix;
        {
            double[][] ZCopy = new double[1][Z.length + 1];
            for(int i = 0;i < Z.length;i++){
                ZCopy[0][i] = Z[i];
            }
            ZCopy[0][ZCopy.length - 1] = 0;
            ZMatrix = DoubleFactory2D.dense.make(ZCopy);
        }
        this.ZT_invM = Algebra.DEFAULT.mult(ZMatrix,invM);
    }

    public OrdinaryKriging(double lag, double[][] u, double[] Z){
        this(lag, u, Z, new SphericalVariogram());
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
        DoubleMatrix2D PMatrix = DoubleFactory2D.dense.make(P);
        DoubleMatrix2D resMatrix = Algebra.DEFAULT.mult(this.ZT_invM, PMatrix);
        return resMatrix.toArray()[0];
    }
}