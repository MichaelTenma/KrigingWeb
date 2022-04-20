package com.example.krigingweb.Interpolation.Core.Kriging;

public class MathUtil {
    public static double distance(double ui_x, double ui_y, double uj_x, double uj_y){
        final double delta_x = ui_x - uj_x;
        final double delta_y = ui_y - uj_y;
        return Math.sqrt(delta_x*delta_x + delta_y*delta_y);
    }

    public static double MAE(double[] predictArray, double[] realArray){
        double MAE = 0;
        for(int i = 0;i < predictArray.length;i++){
            MAE += Math.abs(predictArray[i] - realArray[i]);
        }
        return MAE / predictArray.length;
    }

    public static double RMSE(double[] predictArray, double[] realArray){
        double RMSE = 0;
        for(int i = 0;i < predictArray.length;i++){
            RMSE += Math.pow(predictArray[i] - realArray[i], 2);
        }
        return Math.sqrt(RMSE / predictArray.length);
    }

    public static double MAE(double[] errorArray){
        double MAE = 0;
        for(double error : errorArray){
            MAE += Math.abs(error);
        }
        return MAE / errorArray.length;
    }

    public static double RMSE(double[] errorArray){
        double RMSE = 0;
        for (double error : errorArray) {
            RMSE += Math.pow(error, 2);
        }
        return Math.sqrt(RMSE / errorArray.length);
    }
}
