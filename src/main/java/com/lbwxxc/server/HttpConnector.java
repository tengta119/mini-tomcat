package com.lbwxxc.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author lbwxxc
 * @date 2025/8/24 19:03
 * @description:
 */
public class HttpConnector implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HttpConnector.class);

    int minProcessors = 3;
    int maxProcessors = 10;
    int curProcessor = 0;
    final Deque<HttpProcessor> processors = new ArrayDeque<>();

    @Override
    public void run() {
        ServerSocket serverSocket;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
            for (int i = 0; i < minProcessors; i++) {
                HttpProcessor processor = new HttpProcessor(this);
                processor.start();
                processors.add(processor);
            }
            curProcessor = minProcessors;
            log.info("服务器启动成功");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                HttpProcessor processor = getProcessor();
                if (processor == null) {
                    socket.close();
                    log.error("processor 已耗尽");
                } else {
                    processor.assign(socket);
                }
            } catch (IOException e) {
                log.error("", e);
                break;
            }

        }
    }

    public HttpProcessor getProcessor() {
        synchronized (processors) {
            if (!processors.isEmpty()) {
                return processors.poll();
            } else {
                if (curProcessor < maxProcessors) {
                    curProcessor++;
                    return new HttpProcessor(this);
                }
            }
        }

        return null;
    }

    void recycle(HttpProcessor processor) {
        processors.push(processor);
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
