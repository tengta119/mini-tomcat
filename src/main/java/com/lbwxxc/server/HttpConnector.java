package com.lbwxxc.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author lbwxxc
 * @date 2025/8/24 19:03
 * @description:
 */
public class HttpConnector implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HttpConnector.class);

    @Override
    public void run() {
        ServerSocket serverSocket;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
            log.info("服务器启动成功");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                HttpProcessor httpProcessor = new HttpProcessor();
                httpProcessor.process(socket);
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
