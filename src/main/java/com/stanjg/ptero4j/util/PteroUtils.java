package com.stanjg.ptero4j.util;

import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class PteroUtils {

    public static void log(Object o) {
        System.out.println(o);
    }

    public static void logRequestError(Response response) throws IOException {
        System.out.println("An error occured while requesting "+response.request().method()+" "+response.request().url()+" CODE: "+response.code());
    }

}
