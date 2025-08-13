package com.lbwxxc.server;


import java.io.IOException;
import java.io.InputStream;

/**
 * @author lbwxxc
 * @date 2025/8/13 16:59
 * @description:
 */
public class Request {

    InputStream input;
    String uri;
    public Request(InputStream input) {
        this.input = input;
    }

    public void parse() {

        int i;
        byte[] buffer = new byte[2024];
        try {
            i = input.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) {
            sb.append((char) buffer[j]);
        }

        uri = parseUir(sb.toString());
    }

    public String parseUir(String str) {

        int index1 = str.indexOf(' ');
        int index2 = str.indexOf(' ', index1 + 1);
        if (index1 == -1 || index2 == -1) {
            throw new RuntimeException("请求格式异常");
        }

        return str.substring(index1 + 1, index2);
    }

    public String getUri() {
        return uri;
    }
}
