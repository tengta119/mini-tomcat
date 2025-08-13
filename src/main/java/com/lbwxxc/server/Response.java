package com.lbwxxc.server;


import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author lbwxxc
 * @date 2025/8/13 17:12
 * @description:
 */
public class Response {

    Request request;
    OutputStream out;
    int BUFFER_SIZE = 1024;
    public Response(Request request, OutputStream out) {
        this.request = request;
        this.out = out;
    }

    public void sendStaticResource() {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        try {
            File file = new File(HttpServer.WEB_ROOT, request.getUri());
            if (file.exists()) {
                // 在发送文件内容前，先发送成功的HTTP响应头
                String successHeader = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"  // 注意: 这里可以根据文件类型动态改变
                        + "Content-Length: " + file.length() + "\r\n"
                        + "\r\n"; // 重要的空行，分隔头和体
                out.write(successHeader.getBytes(StandardCharsets.UTF_8));
                fis = new FileInputStream(file);
                int ch = fis.read(bytes, 0, BUFFER_SIZE);
                while (ch != -1) {
                    out.write(bytes, 0, ch);
                    ch = fis.read(bytes, 0, BUFFER_SIZE);
                }
                out.flush();
            } else {
                // file not found
                String errorMessage = """
                        HTTP/1.1 404 File Not Found\r
                        Content-Type: text/html\r
                        Content-Length: 23\r
                        \r
                        <h1>File Not Found</h1>""";
                out.write(errorMessage.getBytes());
            }
        } catch (Exception e) {
            // thrown if cannot instantiate a File object
            System.out.println(e.getMessage());
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException ignored) {

                }
            }
        }
    }
}
