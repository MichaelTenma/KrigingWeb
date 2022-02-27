package com.example.krigingweb.Exception;

import java.util.List;

public class EmptyListException extends Exception{
    public EmptyListException(String name){
        super(name + "不能为null，且不能为空");
    }

    public static <T> void check(String name, List<T> list) throws EmptyListException {
        boolean isError = list == null || list.isEmpty();
        if(isError){
            throw new EmptyListException(name);
        }
    }
}
