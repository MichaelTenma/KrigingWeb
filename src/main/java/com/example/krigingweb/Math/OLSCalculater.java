package com.example.krigingweb.Math;

import jsat.linear.LUPDecomposition;
import jsat.linear.Matrix;
import jsat.linear.SingularValueDecomposition;
import jsat.linear.Vec;

public class OLSCalculater {

    public static Vec OLS(Matrix A, Vec b){
        /* target: Ax = b */
        /* x = inv(A'A)A'b is the unique least squares solution of the system Ax = b. */

        /* A'Ax = A'b */
        Matrix AT = A.transpose();/* A' */
        Matrix ATA = AT.multiply(A);/* A'A */
        Vec ATb = AT.multiply(b);/* ATb */

        /* LUP: x = inv(A'A)A'b */
        LUPDecomposition lup = new LUPDecomposition(ATA);
        Vec xSolution = lup.solve(ATb);
        if(Double.isNaN(lup.det()) || Math.abs(lup.det()) < 1e-5) {
            SingularValueDecomposition svd = new SingularValueDecomposition(ATA);
            xSolution = svd.solve(ATb);
        }
        return xSolution;
    }
}

