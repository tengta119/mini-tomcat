package com.lbwxxc.server;


import java.io.File;

/**
 * @author lbwxxc
 * @date 2025/8/13 17:21
 * @description:
 */
public class HttpServer {

    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";

    public static void main(String[] args) {
        HttpConnector httpConnector = new HttpConnector();
        httpConnector.start();
    }

}
