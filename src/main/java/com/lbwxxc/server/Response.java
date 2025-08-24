package com.lbwxxc.server;


import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author lbwxxc
 * @date 2025/8/13 17:12
 * @description:
 */
public class Response {

    Request request;
    OutputStream out;
    int BUFFER_SIZE = 1024;
    public Response(Request request, OutputStream out) {
        this.request = request;
        this.out = out;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public OutputStream getOut() {
        return out;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public int getBUFFER_SIZE() {
        return BUFFER_SIZE;
    }

    public void setBUFFER_SIZE(int BUFFER_SIZE) {
        this.BUFFER_SIZE = BUFFER_SIZE;
    }
}
