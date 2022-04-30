package com.example.krigingweb.Interpolation.Core;

import com.example.krigingweb.Interpolation.Core.Util.MathUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorEntity {

    @JsonProperty("MAE")
    private Double MAE;
    @JsonProperty("RMSE")
    private Double RMSE;

    public ErrorEntity(double[] errorList){
        this.MAE = MathUtil.MAE(errorList);
        this.RMSE = MathUtil.RMSE(errorList);
    }

    public ErrorEntity(Double MAE, Double RMSE) {
        this.MAE = MAE;
        this.RMSE = RMSE;
    }

//    public static double[] calError(List<DataPointPair<Double>> list, Regressor regressor){
//        double[] errorArray = new double[list.size()];
//        int i = 0;
//        for(DataPointPair<Double> dataPointPair : list){
//            double value = dataPointPair.getPair();
//            double predictValue = regressor.regress(dataPointPair.getDataPoint());
//            errorArray[i] = predictValue - value;
//            i++;
//        }
//        return errorArray;
//    }

    @Override
    public String toString(){
        return String.format("MAE: %f, RMSE: %f", this.MAE, this.RMSE);
    }
}
