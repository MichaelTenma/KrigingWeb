package com.example.krigingweb.Interpolation.Core.Util;

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

    /**
     * 计算点a与点b之间的欧几里得距离（二范数）
     * @param aX 点a的X坐标
     * @param aY 点a的Y坐标
     * @param bX 点b的X坐标
     * @param bY 点b的Y坐标
     * @return 欧几里得距离
     */
    public static double norm2(double aX, double aY, double bX, double bY){
        return Math.sqrt(Math.pow((aX - bX), 2) + Math.pow((aY - bY), 2));
    }

    public static void arrayToString(String label, double[] array){
        StringBuilder text = new StringBuilder(label + " = [");
        for(double num : array){
            text.append(num).append(",");
        }
        text.setCharAt(text.length() - 1, ']');
        System.out.println(text.append(";"));
    }
}
