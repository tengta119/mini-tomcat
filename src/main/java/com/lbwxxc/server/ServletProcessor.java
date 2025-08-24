package com.lbwxxc.server;


import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * @author lbwxxc
 * @date 2025/8/13 23:54
 * @description:
 */
public class ServletProcessor {

    private static final int BUFFER_SIZE = 1024;
    //下面的字符串是当文件没有找到时返回的 404 错误描述
    private final static String fileNotFoundMessage = """
            HTTP/1.1 404 File Not Found\r
            Content-Type: text/html\r
            \r
            <h1>File Not Found</h1>
            """;
    //下面的字符串是正常情况下返回的,根据http协议,里面包含了相应的变量。
    private final static String OKMessage = """
            HTTP/1.1 ${StatusCode} ${StatusName}\r
            Content-Type: ${ContentType}\r
            Server: miniTomcat\r
            Date: ${ZonedDateTime}\r
            \r
            """;

    public void process(Request request, Response response) {
        String uir = request.getUri();
        URLClassLoader loader;

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

        String ServletName = "com.lbwxxc.server.HelloServlet";
        Class<?> servletClass;

        try {
            servletClass = this.getClass().getClassLoader().loadClass(ServletName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        OutputStream out = response.getOut();
        try {
            out.write(composeResponseHead().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Servlet servlet;
        try {
            servlet = (Servlet) servletClass.newInstance();
            servlet.service(request, response);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //拼响应头,填充变量值
    private String composeResponseHead() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("StatusCode", "200");
        headers.put("StatusName", "ok");
        headers.put("ContentType", "text/html;charset=utf-8");
        headers.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));

        return new StrSubstitutor(headers).replace(OKMessage);
    }
}
