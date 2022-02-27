package com.example.krigingweb.Interpolation.Distributor.Exception;

public class EmptyInterpolaterException extends Exception{
    public EmptyInterpolaterException() {
        super("没有可用的插值结点！");
    }

    @FunctionalInterface
    public interface Handler{
        void handle();
    }
}
