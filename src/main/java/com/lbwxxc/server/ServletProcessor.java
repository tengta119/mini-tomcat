package com.lbwxxc.server;


import com.lbwxxc.RandR.HttpRequest;
import com.lbwxxc.RandR.HttpRequestFacade;
import com.lbwxxc.RandR.HttpResponse;
import com.lbwxxc.RandR.HttpResponseFacade;
import org.apache.commons.lang3.text.StrSubstitutor;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URLClassLoader;
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
    private HttpConnector connector;
    public ServletProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    public void process(HttpRequest request, HttpResponse response) {
        this.connector.getContainer().invoke(request, response);
    }

    //拼响应头,填充变量值
    private String composeResponseHead() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("StatusCode", "200");
        headers.put("StatusName", "ok");
        headers.put("ContentType", "text/html;charset=utf-8");
        headers.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
        String headersStr = new StrSubstitutor(headers).replace(OKMessage);
        return headersStr;
    }
}
