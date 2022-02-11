package com.example.krigingweb;

public final class MathUtil {

    public static double[] diff(double[] tArray){
        double[] diffArray = new double[tArray.length - 1];

        double last = tArray[0];
        for(int i = 1;i < tArray.length;i++){
            double current = tArray[i];
            diffArray[i - 1] = current - last;
            last = current;
        }
        return diffArray;
    }

    public static double[] diffSum(double[] tArray){
        double[] diffArray = MathUtil.diff(tArray);
        double[] diffSumArray = new double[diffArray.length];
        double sum = 0;
        for(int i = 0;i < diffArray.length;i++){
            sum += diffArray[i];
            diffSumArray[i] = sum;
        }

        MathUtil.arrayToString("diffArray", diffArray);
        MathUtil.arrayToString("diffSumArray", diffSumArray);
        return diffSumArray;
    }

    public static double avg(int beginIndex, double[] tArray){
        double sum = 0;
        for(int i = beginIndex;i < tArray.length;i++){
            sum += tArray[i];
        }
        return sum / (tArray.length-beginIndex);
    }

    public static double avg(double[] tArray){
        return MathUtil.avg(0, tArray);
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
