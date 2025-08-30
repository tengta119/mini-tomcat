package com.lbwxxc.server;

import com.lbwxxc.utils.CookieTools;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HttpResponse implements HttpServletResponse {
    HttpRequest request;
    OutputStream output;
    PrintWriter writer;

    String contentType = null;
    long contentLength = -1;
    String charset = null;
    String characterEncoding = null;
    String protocol = "HTTP/1.1";
    ArrayList<Cookie> cookies = new ArrayList<>();
    Map<String, String> headers = new ConcurrentHashMap<>();
    String message = getStatusMessage(HttpServletResponse.SC_OK);
    int status = HttpServletResponse.SC_OK;

    public HttpResponse(OutputStream output) {
        this.output = output;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    protected String getStatusMessage(int status) {
        return switch (status) {
            case SC_OK -> ("OK");
            case SC_ACCEPTED -> ("Accepted");
            case SC_BAD_GATEWAY -> ("Bad Gateway");
            case SC_BAD_REQUEST -> ("Bad Request");
            case SC_CONTINUE -> ("Continue");
            case SC_FORBIDDEN -> ("Forbidden");
            case SC_INTERNAL_SERVER_ERROR -> ("Internal Server Error");
            case SC_METHOD_NOT_ALLOWED -> ("Method Not Allowed");
            case SC_NOT_FOUND -> ("Not Found");
            case SC_NOT_IMPLEMENTED -> ("Not Implemented");
            case SC_REQUEST_URI_TOO_LONG -> ("Request URI Too Long");
            case SC_SERVICE_UNAVAILABLE -> ("Service Unavailable");
            case SC_UNAUTHORIZED -> ("Unauthorized");
            default -> ("HTTP Response Status " + status);
        };
    }

    public void sendHeaders() throws IOException {
        PrintWriter outputWriter = getWriter();
        outputWriter.print(this.getProtocol());
        outputWriter.print(" ");
        outputWriter.print(status);
        if (message != null) {
            outputWriter.print(" ");
            outputWriter.print(message);
        }
        outputWriter.print("\r\n");
        if (getContentType() != null) {
            outputWriter.print("Content-Type: " + getContentType() + "\r\n");
        }
        if (getContentLength() >= 0) {
            outputWriter.print("Content-Length: " + getContentLength() + "\r\n");
        }
        Iterator<String> names = headers.keySet().iterator();
        while (names.hasNext()) {
            String name = names.next();
            String value = headers.get(name);
            outputWriter.print(name);
            outputWriter.print(": ");
            outputWriter.print(value);
            outputWriter.print("\r\n");
        }

        HttpSession session = this.request.getSession(false);
        if (session != null) {
            Cookie cookie = new Cookie(DefaultHeaders.JSESSIONID_NAME, session.getId());
            cookie.setMaxAge(-1);
            addCookie(cookie);
        }

        synchronized (cookies) {
            Iterator<Cookie> items = cookies.iterator();
            while (items.hasNext()) {
                Cookie cookie = items.next();
                outputWriter.print(CookieTools.getCookieHeaderName(cookie));
                outputWriter.print(": ");
                StringBuffer sbValue = new StringBuffer();
                CookieTools.getCookieHeaderValue(cookie, sbValue);
                System.out.println("set cookie jsessionid string : "+ sbValue);
                outputWriter.print(sbValue);
                outputWriter.print("\r\n");
            }
        }

        outputWriter.print("\r\n");
        outputWriter.flush();
    }


    private long getContentLength() {
        return this.contentLength;
    }

    private String getProtocol() {
        return this.protocol;
    }

    public OutputStream getOutput() {
        return this.output;
    }

    @Override
    public void flushBuffer() throws IOException {
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return (ServletOutputStream) output;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(output), true);
        return writer;
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public void setBufferSize(int arg0) {
    }

    @Override
    public void setCharacterEncoding(String arg0) {
        this.characterEncoding = arg0;
    }

    @Override
    public void setContentLength(int length) {
        this.contentLength = length;
    }

    @Override
    public void setContentLengthLong(long length) {
        this.contentLength = length;
    }

    @Override
    public void setContentType(String arg0) {
        this.contentType = arg0;
    }

    @Override
    public void setLocale(Locale arg0) {
    }

    @Override
    public void addCookie(Cookie arg0) {
        cookies.add(arg0);
    }

    @Override
    public void addDateHeader(String arg0, long arg1) {
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
        if (name.toLowerCase() == DefaultHeaders.CONTENT_LENGTH_NAME) {
            setContentLength(Integer.parseInt(value));
        }
        if (name.toLowerCase() == DefaultHeaders.CONTENT_TYPE_NAME) {
            setContentType(value);
        }
    }

    @Override
    public void addIntHeader(String arg0, int arg1) {
    }

    @Override
    public boolean containsHeader(String name) {
        return (headers.get(name) != null);
    }

    @Override
    public String encodeRedirectURL(String arg0) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String arg0) {
        return null;
    }

    @Override
    public String encodeURL(String arg0) {
        return null;
    }

    @Override
    public String encodeUrl(String arg0) {
        return null;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public Collection<String> getHeaders(String arg0) {
        return null;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public void sendError(int arg0) throws IOException {
    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {
    }

    @Override
    public void sendRedirect(String arg0) throws IOException {
    }

    @Override
    public void setDateHeader(String arg0, long arg1) {
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
        if (name.toLowerCase() == DefaultHeaders.CONTENT_LENGTH_NAME) {
            setContentLength(Integer.parseInt(value));
        }
        if (name.toLowerCase() == DefaultHeaders.CONTENT_TYPE_NAME) {
            setContentType(value);
        }
    }

    @Override
    public void setIntHeader(String arg0, int arg1) {
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
        this.message = this.getStatusMessage(status);
    }

    @Override
    public void setStatus(int arg0, String arg1) {
    }
}
