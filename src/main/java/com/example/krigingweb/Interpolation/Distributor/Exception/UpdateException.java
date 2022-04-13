package com.example.krigingweb.Interpolation.Distributor.Exception;

public class UpdateException extends Exception{
    public final Exception e;
    public UpdateException(Exception e){
        this.e = e;
    }
}
