package com.example.krigingweb.Interpolation.Core.Kriging;

public class Grouper {

    public static MaxMinResult<Double> maxOrMin(double[] array){
        double min = array[0];
        double max = array[0];

        for(double value : array){
            if(value < min){
                min = value;
            }

            if(value > max){
                max = value;
            }
        }
        return new MaxMinResult<>(min, max);
    }

    /**
     * 通过lag约束semiVariogram
     * @return 点点间最大距离
     */
    public static GrouperResult groupDistance(double[] distanceArray, double[] semiArray, double lag, double lagDistance){
        MaxMinResult<Double> maxMinResult = Grouper.maxOrMin(distanceArray);
        double minDistance = maxMinResult.min;
        double maxDistance = maxMinResult.max;

//        int N = (int) Math.ceil(maxDistance / lag);
        int N = (int)Math.ceil(lagDistance / lag);
//        System.out.println("N: " + N);

        double[] tempDistanceArray = new double[N];
        double[] tempSemiArray = new double[N];

        for(int i = 0;i < N;i++){
            tempDistanceArray[i] = 0;
            tempSemiArray[i] = 0;
        }

        for(int i = 0;i < distanceArray.length;i++){
            double distance = distanceArray[i] - minDistance;
            int index = (int)(distance / lag);
            if(index < N){
                tempDistanceArray[index]++;
                tempSemiArray[index] += (semiArray[i] - tempSemiArray[index]) / tempDistanceArray[index];
            }
        }

        double beginDistance = minDistance + lag / 2;
        for(int i = 0; i < N;i++){
            tempDistanceArray[i] = beginDistance;
            beginDistance += lag;
        }

        return new GrouperResult(tempDistanceArray, tempSemiArray);
    }

    public static double groupSemi(double[] semiArray){
        MaxMinResult<Double> maxMinResult = Grouper.maxOrMin(semiArray);
        double minSemi = maxMinResult.min;
        double maxSemi = maxMinResult.max;

        double lag = 10;
        int N = (int)Math.ceil((maxSemi - minSemi) / lag);

        int[] countArray = new int[N];
        for(int i = 0;i < semiArray.length;i++){
            double semi = semiArray[i] - minSemi;
            int index = (int)(semi / lag);
            countArray[index]++;
        }

        int maxCount = countArray[0];
        int maxIndex = 0;
        for(int index = 0;index < countArray.length;index++){
            if(maxCount < countArray[index]){
                maxCount = countArray[index];
                maxIndex = index;
            }
        }
        return minSemi + maxIndex * lag + lag / 2;
    }
}

class GrouperResult{
    public final double[] distanceArray;
    public final double[] semiArray;
    GrouperResult(double[] distanceArray, double[] semiArray) {
        this.distanceArray = distanceArray;
        this.semiArray = semiArray;
    }
}

class MaxMinResult<T>{
    public final T min;
    public final T max;

    MaxMinResult(T min, T max) {
        this.min = min;
        this.max = max;
    }
}

