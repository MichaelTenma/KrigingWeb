package com.example.krigingweb.Entity;

import com.example.krigingweb.Math.MathUtil;
import jsat.classifiers.DataPointPair;
import jsat.regression.Regressor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class ErrorEntity {
    private Double MAE;
    private Double RMSE;

    public ErrorEntity(List<Double> testErrorList){
        this.MAE = MathUtil.MAE(testErrorList);
        this.RMSE = MathUtil.RMSE(testErrorList);
    }

    public static List<Double> calError(List<DataPointPair<Double>> list, Regressor regressor){
        List<Double> errorList = new ArrayList<>(list.size());

        for(DataPointPair<Double> dataPointPair : list){
            double value = dataPointPair.getPair();
            double predictValue = regressor.regress(dataPointPair.getDataPoint());
            errorList.add(predictValue - value);
        }
        return errorList;
    }

    @Override
    public String toString(){
        return String.format("MAE: %f, RMSE: %f", this.MAE, this.RMSE);
    }
}
