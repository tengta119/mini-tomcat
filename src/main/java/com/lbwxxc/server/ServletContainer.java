package com.lbwxxc.server;

import com.lbwxxc.HttpServer;
import com.lbwxxc.RandR.HttpRequest;
import com.lbwxxc.RandR.HttpRequestFacade;
import com.lbwxxc.RandR.HttpResponse;
import com.lbwxxc.RandR.HttpResponseFacade;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServletContainer {
    HttpConnector connector;
    ClassLoader loader;

    Map<String, String> servletClsMap = new ConcurrentHashMap<>();
    Map<String, Servlet> servletInstanceMap = new ConcurrentHashMap<>();

    public ServletContainer() {
        URL[] urls = new URL[1];
        URLStreamHandler streamHandler = null;
        File classPath = new File("target/classes/com/lbwxxc/test");
        try {
            String repository = (new URL("file", null,  classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void invoke(HttpRequest request, HttpResponse response) {
        Servlet servlet = null;
        ClassLoader loader = getLoader();
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        String servletClassName = servletName;
        servlet = servletInstanceMap.get(servletName);


        if (servlet == null) {
            Class<?> servletClass = null;
            try {
                servletClass =  loader.loadClass("com.lbwxxc.test.HelloServlet");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                servlet = (Servlet) servletClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            servletClsMap.put(servletName, servletClassName);
            servletInstanceMap.put(servletName, servlet);
        }

        try {
            servlet.init(null);
            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
            System.out.println("Call service()");
            servlet.service(requestFacade, responseFacade);
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public Map<String, String> getServletClsMap() {
        return servletClsMap;
    }

    public void setServletClsMap(Map<String, String> servletClsMap) {
        this.servletClsMap = servletClsMap;
    }

    public Map<String, Servlet> getServletInstanceMap() {
        return servletInstanceMap;
    }

    public void setServletInstanceMap(Map<String, Servlet> servletInstanceMap) {
        this.servletInstanceMap = servletInstanceMap;
    }
}
