package com.example.krigingweb.Exception;

public class EmptyIPException extends Exception{
    public EmptyIPException() {
        super("无法获取到插值结点的IP地址！");
    }

    public static void check(String ip) throws EmptyIPException {
        boolean isError = ip == null || ip.equals("");
        if(isError){
            throw new EmptyIPException();
        }
    }
}
