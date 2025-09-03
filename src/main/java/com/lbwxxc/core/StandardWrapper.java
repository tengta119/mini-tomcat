package com.lbwxxc.core;

import com.lbwxxc.Logger;
import com.lbwxxc.Request;
import com.lbwxxc.Response;
import com.lbwxxc.Wrapper;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

public class StandardWrapper extends ContainerBase implements Wrapper {
    private Servlet instance = null;
    private String servletClass;
    Logger logger;

    public StandardWrapper(String servletClass, StandardContext parent) {
        super();
        pipeline.setBasic(new StandardWrapperValve());
        this.parent = parent;
        this.servletClass = servletClass;
        loadServlet();
        //loadServlet();
    }

    public ClassLoader getLoader() {
        if (loader != null)
            return loader;
        return parent.getLoader();
    }

    @Override
    public int getLoadOnStartup() {
        return 0;
    }

    @Override
    public void setLoadOnStartup(int value) {

    }

    public String getServletClass() {
        return servletClass;
    }
    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    @Override
    public void addInitParameter(String name, String value) {

    }

    @Override
    public Servlet allocate() throws ServletException {
        return null;
    }

    @Override
    public String findInitParameter(String name) {
        return "";
    }

    @Override
    public String[] findInitParameters() {
        return new String[0];
    }

    @Override
    public void load() throws ServletException {

    }

    @Override
    public void removeInitParameter(String name) {

    }

    public StandardContext getParent() {
        return (StandardContext) parent;
    }

    public void setParent(StandardContext container) {
        parent = container;
    }
    public Servlet getServlet(){
        return this.instance;
    }

    public Servlet loadServlet()  {
        if (instance != null)
            return instance;
        Servlet servlet = null;
        String actualClass = servletClass;
        if (actualClass == null) {

        }
        ClassLoader classLoader = getLoader();
        Class classClass = null;
        try {
            if (classLoader!=null) {
                classClass = classLoader.loadClass(actualClass);
            }
        }
        catch (ClassNotFoundException e) {
            log(e.getMessage());
        }
        try {
            servlet = (Servlet) classClass.newInstance();
        }
        catch (Throwable e) {
            log(e.getMessage());
        }

        try {
            servlet.init(null);
        }
        catch (Throwable f) {
            log(f.getMessage());
        }
        instance = servlet;

        try {
            Class<?> aClass = getLoader().loadClass("com.lbwxxc.test.HelloServlet");
            instance = (Servlet) aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return servlet;
    }

    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        super.invoke(request, response);
    }

    public Servlet getInstance() {
        return instance;
    }

    public void setInstance(Servlet instance) {
        this.instance = instance;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String getInfo() {
        return "";
    }
}
