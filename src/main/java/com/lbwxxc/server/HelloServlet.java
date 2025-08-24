package com.lbwxxc.server;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author lbwxxc
 * @date 2025/8/14 00:07
 * @description:
 */
public class HelloServlet implements Servlet {

    @Override
    public void service(Request req, Response res) {
        String doc = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"><title>Test</title></head>
                <body bgcolor="#f0f0f0">
                <h1 align="center">Hello World 你好</h1>
                """;
        try {
            res.getOut().write(doc.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
