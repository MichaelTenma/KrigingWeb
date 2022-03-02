package com.example.krigingweb.Interpolation.Basic;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpUtil {
//    public static final HttpHeaders urlEncodedHeaders;
    public static final HttpHeaders jsonHeaders;
    static {
//        urlEncodedHeaders = new HttpHeaders();
//        urlEncodedHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }
}
