package com.lbwxxc.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author lbwxxc
 * @date 2025/8/24 19:01
 * @description:
 */
public class HttpProcessor {

    private static final Logger log = LoggerFactory.getLogger(HttpProcessor.class);

    public void process(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            Request request = new Request(inputStream);
            request.parse();

            OutputStream outputStream = socket.getOutputStream();
            Response response = new Response(request, outputStream);

            if (request.getUri().startsWith("/servlet/")) {
                log.info("访问动态资源");
                ServletProcessor servletProcessor = new ServletProcessor();
                servletProcessor.process(request, response);
            } else {
                StaticResourceProcessor staticResourceProcessor = new StaticResourceProcessor();
                staticResourceProcessor.process(request, response);
            }

            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
