package com.lbwxxc.server;


import org.apache.commons.lang3.text.StrSubstitutor;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
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

    public void process(HttpRequest request, HttpResponse response) {
        String uir = request.getUri();
        String ServletName = uir.substring(uir.lastIndexOf('/') + 1);
        URLClassLoader loader = HttpConnector.loader;


        ServletName = "com.lbwxxc.test.HelloServlet";
        Class<?> servletClass;
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            servletClass = classLoader.loadClass(ServletName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        PrintWriter writer;
        try {
            writer = response.getWriter();
            writer.println(composeResponseHead());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpRequestFacade httpRequestFacade = new HttpRequestFacade(request);
        HttpResponseFacade httpResponseFacade = new HttpResponseFacade(response);
        Servlet servlet;
        try {
            servlet = (Servlet) servletClass.newInstance();
            servlet.service(httpRequestFacade, httpResponseFacade);
        } catch (InstantiationException | IllegalAccessException | ServletException | IOException e) {
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
