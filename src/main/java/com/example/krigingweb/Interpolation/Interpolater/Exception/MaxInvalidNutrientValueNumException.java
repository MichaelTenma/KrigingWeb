package com.example.krigingweb.Interpolation.Interpolater.Exception;

import com.example.krigingweb.Interpolation.Core.TaskData;

/**
 * 插值结果中含有过多的无效值，判定本次插值结果无效！
 */
public class MaxInvalidNutrientValueNumException extends Exception{
    public MaxInvalidNutrientValueNumException(){
        super("插值结果中含有过多的无效值，判定本次插值结果无效！");
    }

    @FunctionalInterface
    public interface Handler{
        void handle();
    }
}
