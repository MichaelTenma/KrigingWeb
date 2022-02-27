package com.example.krigingweb.Exception;

public class EmptyException extends Exception{
    public EmptyException(String name){
        super(name + "不能为null，且不能为空！");
    }

    public static <T> void check(String name, T t) throws EmptyException {
        boolean isError = t == null || t.toString().equals("");
        if(isError){
            throw new EmptyException(name);
        }
    }
}
