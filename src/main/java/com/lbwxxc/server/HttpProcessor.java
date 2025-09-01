package com.lbwxxc.server;


import com.lbwxxc.RandR.HttpRequest;
import com.lbwxxc.RandR.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author lbwxxc
 * @date 2025/8/24 19:01
 * @description:
 */
public class HttpProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HttpProcessor.class);

    Socket socket;
    boolean available = false;
    HttpConnector connector;
    private int serverPort = 0;
    private boolean keepAlive = true;
    private boolean http11 = true;

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (true) {
            Socket socket = await();
            if (socket == null) {
                continue;
            }
            process(socket);
            connector.recycle(this);
        }
    }

    public void process(Socket socket) {
        InputStream inputStream;
        OutputStream outputStream;
        try {

            socket.setSoTimeout(10);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            HttpRequest httpRequest = new HttpRequest(inputStream);
            httpRequest.parse(socket);
            if (httpRequest.getSessionid() == null || httpRequest.getSessionid().isEmpty()) {
                // 尝试获取 session，如果没有则创建
                httpRequest.getSession(true);
            }
            HttpResponse httpResponse = new HttpResponse(outputStream);
            httpResponse.setRequest(httpRequest);
            httpResponse.sendHeaders();
            if (httpRequest.getUri().startsWith("/servlet/")) {
                log.info("访问动态资源");
                ServletProcessor servletProcessor = new ServletProcessor();
                servletProcessor.process(httpRequest, httpResponse);
            } else {
                StaticResourceProcessor staticResourceProcessor = new StaticResourceProcessor();
                staticResourceProcessor.process(httpRequest, httpResponse);
            }

            finishResponse(httpResponse);
            log.info("response header connection----- {}", httpRequest.getHeader("connection"));
            keepAlive = httpRequest.getHeader("connection") != null
                    && httpRequest.getHeader("connection").equalsIgnoreCase("keep-alive");


            socket.close();
            socket = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void finishResponse(HttpResponse response) {
        response.finishResponse();
    }

    synchronized void assign(Socket socket) {
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.socket = socket;
        available = true;
        notifyAll();
    }

    private synchronized Socket await() {
        while (!available) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Socket socket = this.socket;
        available = false;
        notifyAll();
        return (socket);
    }
}
