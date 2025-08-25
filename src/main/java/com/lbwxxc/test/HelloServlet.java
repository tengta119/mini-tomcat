package com.lbwxxc.test;


import javax.servlet.*;
import java.io.IOException;

/**
 * @author lbwxxc
 * @date 2025/8/14 00:07
 * @description:
 */
public class HelloServlet implements Servlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) {
        String doc = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"><title>Test</title></head>
                <body bgcolor="#f0f0f0">
                <h1 align="center">Hello World 你好</h1>
                """;
        try {
            servletResponse.getWriter().println(doc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getServletInfo() {
        return "";
    }

    @Override
    public void destroy() {

    }

}
