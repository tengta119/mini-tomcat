package com.lbwxxc.server;


import com.lbwxxc.HttpServer;
import com.lbwxxc.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
    // sessions map 存放 session
    public static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    //一个全局的class loader
    public static URLClassLoader loader = null;
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

        try {
            URL[] urls = new URL[1];
            File classPath = new File(HttpServer.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            URLStreamHandler urlStreamHandler = null;
            urls[0] = new URL(null, repository, urlStreamHandler);
            loader = new URLClassLoader(urls);
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

    public static Session createSession() {
        Session session = new Session();
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        String sessionId = generateSessionId();
        session.setId(sessionId);
        sessions.put(sessionId, session);
        return (session);
    }

    protected static synchronized String generateSessionId() {
        Random random = new Random();
        long seed = System.currentTimeMillis();
        random.setSeed(seed);
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            byte b1 = (byte) ((aByte & 0xf0) >> 4);
            byte b2 = (byte) (aByte & 0x0f);
            if (b1 < 10)
                result.append((char) ('0' + b1));
            else
                result.append((char) ('A' + (b1 - 10)));
            if (b2 < 10)
                result.append((char) ('0' + b2));
            else
                result.append((char) ('A' + (b2 - 10)));
        }
        return (result.toString());
    }

    void recycle(HttpProcessor processor) {
        processors.push(processor);
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
