package com.lbwxxc.server;


import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * @author lbwxxc
 * @date 2025/8/13 23:30
 * @description:
 */
public class StaticResourceProcessor {

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
            Content-Length: ${ContentLength}\r
            Server: miniTomcat\r
            Date: ${ZonedDateTime}\r
            \r
            """;

    public void process(HttpRequest request, HttpResponse response) {
        try (FileInputStream fis = new FileInputStream(new File(HttpServer.WEB_ROOT, request.getUri()))) {

            OutputStream out = response.getOutput();
            out.write(composeResponseHead(fis.available()).getBytes(StandardCharsets.UTF_8));
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //拼响应头,填充变量值
    private String composeResponseHead(long length) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("StatusCode", "200");
        headers.put("StatusName", "ok");
        headers.put("ContentType", "text/html;charset=utf-8");
        headers.put("ContentLength", Long.toString(length));
        headers.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));

        return new StrSubstitutor(headers).replace(OKMessage);
    }
}
