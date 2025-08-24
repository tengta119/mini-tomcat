package com.lbwxxc.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author lbwxxc
 * @date 2025/8/13 17:21
 * @description:
 */
public class HttpServer {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);


    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";

    public static void main(String[] args) {
        HttpServer httpServer = new HttpServer();
        System.out.println(WEB_ROOT);
        httpServer.await();
    }

    public void await() {
        ServerSocket serverSocket;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
            log.info("服务器启动成功");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            Socket socket;
            InputStream inputStream;
            OutputStream outputStream;
            try {
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                Request request = new Request(inputStream);
                request.parse();

                outputStream = socket.getOutputStream();
                Response response = new Response(request, outputStream);

                if (request.getUri().startsWith("/servlet/")) {
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
}
