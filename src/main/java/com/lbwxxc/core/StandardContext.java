package com.lbwxxc.core;

import com.lbwxxc.Context;
import com.lbwxxc.Logger;
import com.lbwxxc.Wrapper;
import com.lbwxxc.connect.http.HttpRequestImpl;
import com.lbwxxc.connect.HttpRequestFacade;
import com.lbwxxc.connect.http.HttpResponseImpl;
import com.lbwxxc.connect.HttpResponseFacade;
import com.lbwxxc.connect.http.HttpConnector;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardContext extends ContainerBase implements Context {
    HttpConnector connector;
    ClassLoader loader;
    Map<String, String> servletClsMap = new ConcurrentHashMap<>();
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();

    public StandardContext() {
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
        log("Container created.");
    }

    @Override
    public void invoke(HttpServletRequest request, HttpServletResponse response) {

    }

    public void invoke(HttpRequestImpl request, HttpResponseImpl response) {
        StandardWrapper servlet = null;
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
                servlet = new StandardWrapper(servletClassName, this);
                servlet.setInstance((Servlet) servletClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            servletClsMap.put(servletName, servletClassName);
            servletInstanceMap.put(servletName, servlet);
        }

        try {
            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
            System.out.println("Call service()");
            servlet.invoke(requestFacade, responseFacade);
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

    @Override
    public String getInfo() {
        return "Minit Servlet Context, vesion 0.1";
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

    public Map<String, StandardWrapper> getServletInstanceMap() {
        return servletInstanceMap;
    }

    public void setServletInstanceMap(Map<String, StandardWrapper> servletInstanceMap) {
        this.servletInstanceMap = servletInstanceMap;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public void setDisplayName(String displayName) {

    }

    @Override
    public String getDocBase() {
        return "";
    }

    @Override
    public void setDocBase(String docBase) {

    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int timeout) {

    }

    @Override
    public String getWrapperClass() {
        return "";
    }

    @Override
    public void setWrapperClass(String wrapperClass) {

    }

    @Override
    public Wrapper createWrapper() {
        return null;
    }

    @Override
    public String findServletMapping(String pattern) {
        return "";
    }

    @Override
    public String[] findServletMappings() {
        return new String[0];
    }

    @Override
    public void reload() {

    }
}
