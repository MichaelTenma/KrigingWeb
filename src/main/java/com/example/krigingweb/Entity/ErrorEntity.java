package com.example.krigingweb.Entity;

import com.example.krigingweb.Math.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
