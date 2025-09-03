package com.lbwxxc.connect.http;

import com.lbwxxc.*;
import com.lbwxxc.session.StandardSessionFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.ServletContext;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HttpRequestImpl implements HttpServletRequest, Request {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestImpl.class);

    private InputStream input;
    private SocketInputStream sis;
    private String uri;
    // 查询参数
    private String queryString;
    InetAddress address;
    int port;
    protected HashMap<String, String> headers = new HashMap<>();
    protected Map<String, String[]> parameters = new ConcurrentHashMap<>();
    HttpRequestLine requestLine = new HttpRequestLine();

    Cookie[] cookies;
    HttpSession session;
    String sessionid;
    StandardSessionFacade standardSessionFacade;
    boolean parsed = false;
    HttpResponseImpl httpResponseImpl;
    public HttpRequestImpl(InputStream input) {
        this.input = input;
        this.sis = new SocketInputStream(this.input, 2048);
    }

    public void parse(Socket socket) {
        try {
            parseConnection(socket);
            this.sis.readRequestLine(requestLine);
            // 解析请求行，并判断是非存在 cookie
            parseRequestLine();
            // 解析请求头
            parseHeaders();
            // 解析请请求体
            parseParameters();

        } catch (IOException | ServletException e) {
            log.error(e.getMessage());
        }
    }

    public void parseRequestLine() {
        int queryStart = requestLine.indexOf("?");
        if (queryStart >= 0) {
            queryString = new String(requestLine.uri, queryStart + 1, requestLine.uriEnd - queryStart - 1);
            uri = new String(requestLine.uri, 0, queryStart);
            int semicolon = uri.indexOf(DefaultHeaders.JSESSIONID_NAME);
            if (semicolon >= 0) {
                sessionid = uri.substring(semicolon + DefaultHeaders.JSESSIONID_NAME.length());
                uri = uri.substring(0, semicolon);
            }
        } else {
            queryString = null;
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
            String tmp = ";" + DefaultHeaders.JSESSIONID_NAME + "=";
            int semicolon = uri.indexOf(tmp);
            if (semicolon >= 0) {
                sessionid = uri.substring(semicolon + tmp.length());
                uri = uri.substring(0, semicolon);
            }
        }
    }


    protected void parseParameters() {
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = "utf-8";
        }
        String qString = queryString;
        if (qString != null) {
            byte[] bytes = qString.getBytes();
            parseParameters(parameters, bytes, encoding);
        }

        String contentType = getContentType();
        if (contentType == null) {
            contentType = "";
        }
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }

        if ("POST".equals(getMethod()) && (getContentLength() > 0
                && "application/x-www-form-urlencoded".equals(contentType))) {
            int max = getContentLength();
            int len = 0;
            byte[] buf = new byte[max];
            ServletInputStream is = getInputStream();
            while (len < max) {
                try {
                    int next = is.readLine(buf, len, max - len);
                    if (next < 0) {
                        is.close();
                        break;
                    }
                    len += next;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            parseParameters(parameters, buf, encoding);
        }
    }

    public void parseParameters(Map<String,String[]> map, byte[] data, String encoding) {
        if (parsed) {
            return;
        }

        if (data != null && data.length > 0) {
            int pos = 0;
            int ix = 0;
            int ox = 0;
            String key = null;
            String value = null;

            // 解析参数串,处理特殊字符
            while (ix <  data.length) {
                byte c = data[ix++];
                switch ((char) c) {
                    case '&' -> {
                        value = new String(data, 0, ox);
                        if (key != null) {
                            putMapEntry(map, key, value);
                            key = null;
                        }
                        ox = 0;
                    }

                    case '=' -> {
                        key = new String(data, 0, ox);
                        ox = 0;
                    }

                    case '+' -> data[ox++] = (byte) ' ';

                    case '%' -> data[ox++] = (byte) ((convertHexDigit(data[ix++]) << 4) + convertHexDigit(data[ix++]));

                    default -> data[ox++] = (byte) c;
                }
            }

            if (key != null) {
                value = new String(data, 0, ox);
                putMapEntry(map, key, value);
            }
        }

        parsed = true;
    }

    private void parseConnection(Socket socket) {
        address = socket.getInetAddress();
        port = socket.getPort();
    }

    private void parseHeaders() throws IOException, ServletException {
        while (true) {
            HttpHeader header = new HttpHeader();
            sis.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException("httpProcessor.parseHeaders.colon");
                }
            }
            String name = new String(header.name,0, header.nameEnd);
            String value = new String(header.value, 0, header.valueEnd);
            name = name.toLowerCase();
            // Set the corresponding request headers
            switch (name) {
                case DefaultHeaders.ACCEPT_LANGUAGE_NAME -> headers.put(name, value);
                case DefaultHeaders.CONTENT_LENGTH_NAME -> headers.put(name, value);
                case DefaultHeaders.CONTENT_TYPE_NAME -> headers.put(name, value);
                case DefaultHeaders.HOST_NAME -> headers.put(name, value);
                case DefaultHeaders.CONNECTION_NAME -> headers.put(name, value);
                case DefaultHeaders.TRANSFER_ENCODING_NAME -> headers.put(name, value);
                case DefaultHeaders.COOKIE_NAME -> {
                    headers.put(name, value);
                    this.cookies = parseCookieHeader(value);
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("jsessionid")) {
                            this.sessionid = cookie.getValue();
                        }
                    }
                }
                default -> headers.put(name, value);
            }
        }
    }

    //解析Cookie头,格式为: key1=value1;key2=value2
    public  Cookie[] parseCookieHeader(String header) {
        if ((header == null) || (header.isEmpty()) )
            return (new Cookie[0]);
        ArrayList<Cookie> cookieal = new ArrayList<>();
        while (!header.isEmpty()) {
            int semicolon = header.indexOf(';');
            if (semicolon < 0)
                semicolon = header.length();
            if (semicolon == 0)
                break;

            String token = header.substring(0, semicolon);
            if (semicolon < header.length())
                header = header.substring(semicolon + 1);
            else
                header = "";

            try {
                int equals = token.indexOf('=');
                if (equals > 0) {
                    String name = token.substring(0, equals).trim();
                    String value = token.substring(equals+1).trim();
                    cookieal.add(new Cookie(name, value));
                }
            } catch (Throwable e) {
            }
        }
        return cookieal.toArray (new Cookie[0]);
    }

    //给key设置新值,多值用数组来存储
    private static void putMapEntry( Map<String,String[]> map, String name, String value) {
        String[] oldValues = map.get(name);
        String[] newValues;

        if (oldValues == null) {
            newValues = new String[]{value};
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }

        map.put(name, newValues);
    }

    //十六进制字符到数字的转换
    private byte convertHexDigit(byte b) {
        if ((b >= '0') && (b <= '9')) return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte)(b - 'A' + 10);
        return 0;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public HttpServletMapping getHttpServletMapping() {
        return HttpServletRequest.super.getHttpServletMapping();
    }

    @Override
    public PushBuilder newPushBuilder() {
        return HttpServletRequest.super.newPushBuilder();
    }

    @Override
    public Map<String, String> getTrailerFields() {
        return HttpServletRequest.super.getTrailerFields();
    }

    @Override
    public boolean isTrailerFieldsReady() {
        return HttpServletRequest.super.isTrailerFieldsReady();
    }

    @Override
    public String getAuthType() {
        return "";
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return headers.get(s) == null ? "" : headers.get(s);
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    @Override
    public String getMethod() {
        return new String(requestLine.method, 0, requestLine.methodEnd);
    }

    @Override
    public String getPathInfo() {
        return "";
    }

    @Override
    public String getPathTranslated() {
        return "";
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return "";
    }

    @Override
    public String getRemoteUser() {
        return "";
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return "";
    }

    @Override
    public String getRequestURI() {
        return "";
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean b) {
        if (standardSessionFacade != null)
            return standardSessionFacade;
        if (sessionid != null) {
            session = HttpConnector.sessions.get(sessionid);
            if (session != null) {
                standardSessionFacade = new StandardSessionFacade(session);
                return standardSessionFacade;
            } else {
                session = HttpConnector.createSession();
                standardSessionFacade = new StandardSessionFacade(session);
                return standardSessionFacade;
            }
        } else {
            session = HttpConnector.createSession();
            standardSessionFacade = new StandardSessionFacade(session);
            sessionid = session.getId();
            return standardSessionFacade;
        }
    }

    public String getSessionid() {
        return sessionid;
    }

    @Override
    public Connector getConnector() {
        return null;
    }

    @Override
    public void setConnector(Connector connector) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public String getInfo() {
        return "";
    }

    @Override
    public ServletRequest getRequest() {
        return null;
    }

    @Override
    public Response getResponse() {
        return null;
    }

    @Override
    public void setResponse(Response response) {

    }

    @Override
    public Socket getSocket() {
        return null;
    }

    @Override
    public void setSocket(Socket socket) {

    }

    @Override
    public InputStream getStream() {
        return null;
    }

    public void setStream(InputStream input) {
        this.input = input;
        this.sis = new SocketInputStream(this.input, 2028);
    }

    @Override
    public Wrapper getWrapper() {
        return null;
    }

    @Override
    public void setWrapper(Wrapper wrapper) {

    }

    @Override
    public ServletInputStream createInputStream() throws IOException {
        return null;
    }

    @Override
    public void finishRequest() throws IOException {

    }

    @Override
    public void recycle() {

    }

    @Override
    public void setContentLength(int length) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setProtocol(String protocol) {

    }

    @Override
    public void setRemoteAddr(String remote) {

    }

    @Override
    public void setScheme(String scheme) {

    }

    @Override
    public void setServerPort(int port) {

    }

    public void setResponse(HttpResponseImpl response) {
        this.httpResponseImpl = response;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return "";
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) {
        return false;
    }

    @Override
    public void login(String s, String s1) {

    }

    @Override
    public void logout() {

    }

    @Override
    public Collection<Part> getParts() {
        return List.of();
    }

    @Override
    public Part getPart(String s) {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return "";
    }

    @Override
    public void setCharacterEncoding(String s) {

    }

    @Override
    public int getContentLength() {
        try {
            return sis.is.available();
        } catch (IOException e) {
            log.error("读取 httpRequest 大小时发送错误: {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return headers.get(DefaultHeaders.CONTENT_TYPE_NAME);
    }

    @Override
    public ServletInputStream getInputStream() {
        return sis;
    }

    @Override
    public String getParameter(String s) {
        return "";
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Map.of();
    }

    @Override
    public String getProtocol() {
        return "";
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
    public String getServerName() {
        return "";
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return "";
    }

    @Override
    public String getRemoteHost() {
        return "";
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return "";
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return "";
    }

    @Override
    public String getLocalAddr() {
        return "";
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}
