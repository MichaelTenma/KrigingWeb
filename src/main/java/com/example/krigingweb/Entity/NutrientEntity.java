package com.example.krigingweb.Entity;

import com.example.krigingweb.Interpolation.Core.ErrorEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutrientEntity {
    private Double nutrient;
    private ErrorEntity testError;

    public boolean couldBeUpdate(){
        boolean isFail = Double.isNaN(this.nutrient) || Double.isInfinite(this.nutrient);
        return !isFail;
    }
}
