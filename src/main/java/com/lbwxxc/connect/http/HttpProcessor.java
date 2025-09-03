package com.lbwxxc.connect.http;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
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

            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            HttpRequestImpl httpRequestImpl = new HttpRequestImpl(inputStream);
            httpRequestImpl.parse(socket);
            if (httpRequestImpl.getSessionid() == null || httpRequestImpl.getSessionid().isEmpty()) {
                // 尝试获取 session，如果没有则创建
                httpRequestImpl.getSession(true);
            }
            HttpResponseImpl httpResponseImpl = new HttpResponseImpl(outputStream);
            httpResponseImpl.setRequest(httpRequestImpl);
            httpResponseImpl.sendHeaders();
            if (httpRequestImpl.getUri().startsWith("/servlet/")) {
                log.info("访问动态资源");
                ServletProcessor servletProcessor = new ServletProcessor(connector);
                servletProcessor.process(httpRequestImpl, httpResponseImpl);
            } else {
                StaticResourceProcessor staticResourceProcessor = new StaticResourceProcessor();
                staticResourceProcessor.process(httpRequestImpl, httpResponseImpl);
            }

            finishResponse(httpResponseImpl);
            log.info("response header connection----- {}", httpRequestImpl.getHeader("connection"));
            keepAlive = httpRequestImpl.getHeader("connection") != null
                    && httpRequestImpl.getHeader("connection").equalsIgnoreCase("keep-alive");


            socket.close();
            socket = null;
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }

    private void finishResponse(HttpResponseImpl response) {
        response.finishResponse();
    }

    public synchronized void assign(Socket socket) {
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
