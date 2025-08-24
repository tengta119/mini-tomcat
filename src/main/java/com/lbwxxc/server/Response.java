package com.lbwxxc.server;


import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * @author lbwxxc
 * @date 2025/8/13 17:12
 * @description:
 */
public class Response implements ServletResponse {

    Request request;
    OutputStream output;
    int BUFFER_SIZE = 1024;
    PrintWriter printWriter;
    public Response(Request request, OutputStream out) {
        this.request = request;
        this.output = out;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public OutputStream getOut() {
        return output;
    }

    public void setOut(OutputStream out) {
        this.output = out;
    }

    public int getBUFFER_SIZE() {
        return BUFFER_SIZE;
    }

    public void setBUFFER_SIZE(int BUFFER_SIZE) {
        this.BUFFER_SIZE = BUFFER_SIZE;
    }

    @Override
    public String getCharacterEncoding() {
        return "";
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (printWriter == null) {
            printWriter = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
        }
        return printWriter;
    }

    @Override
    public void setCharacterEncoding(String s) {

    }

    @Override
    public void setContentLength(int i) {

    }

    @Override
    public void setContentLengthLong(long l) {

    }

    @Override
    public void setContentType(String s) {

    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
