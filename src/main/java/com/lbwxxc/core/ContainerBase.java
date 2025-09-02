package com.lbwxxc.core;


import com.lbwxxc.Container;
import com.lbwxxc.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lbwxxc
 * @date 2025/9/2 16:39
 * @description:
 */
public abstract class ContainerBase implements Container {
    protected final Map<String, Container> children = new ConcurrentHashMap<>();
    protected ClassLoader loader = null;
    protected String name = null;
    protected Container parent = null;
    Logger logger = null;
    public abstract String getInfo();
    public ClassLoader getLoader() {
        if (loader != null)
            return (loader);
        if (parent != null)
            return (parent.getLoader());
        return (null);
    }

    public synchronized void setLoader(ClassLoader loader) {
        ClassLoader oldLoader = this.loader;
        if (oldLoader == loader) {
            return;
        }
        this.loader = loader;
    }

    protected void log(String message) {
        Logger logger = getLogger();
        if (logger != null)
            logger.log(logName() + ": " + message);
        else
            System.out.println(logName() + ": " + message);
    }


    protected void log(String message, Throwable throwable) {
        Logger logger = getLogger();
        if (logger != null)
            logger.log(logName() + ": " + message, throwable);
        else {
            System.out.println(logName() + ": " + message + ": " + throwable);
            throwable.printStackTrace(System.out);
        }

    }

    protected String logName() {
        String className = this.getClass().getName();
        int period = className.lastIndexOf(".");
        if (period >= 0)
            className = className.substring(period + 1);
        return (className + "[" + getName() + "]");
    }

    public String getName() {
        return (name);
    }


    public void setName(String name) {
        this.name = name;
    }

    public Container getParent() {
        return (parent);
    }


    public void setParent(Container container) {
        Container oldParent = this.parent;
        this.parent = container;
    }


    public void addChild(Container child) {
        addChildInternal(child);
    }

    private void addChildInternal(Container child) {
        synchronized(children) {
            if (children.get(child.getName()) != null)
                throw new IllegalArgumentException("addChild:  Child name '" +
                        child.getName() +
                        "' is not unique");
            child.setParent((Container) this);  // May throw IAE
            children.put(child.getName(), child);
        }
    }

    public Container findChild(String name) {
        if (name == null)
            return (null);
        synchronized (children) {       // Required by post-start changes
            return ((Container) children.get(name));
        }

    }

    @Override
    public Logger getLogger() {
        if (logger != null)
            return (logger);
        if (parent != null)
            return (parent.getLogger());
        return (null);
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Container[] findChildren() {

        synchronized (children) {
            Container results[] = new Container[children.size()];
            return ((Container[]) children.values().toArray(results));
        }

    }

    public void removeChild(Container child) {

        synchronized(children) {
            if (children.get(child.getName()) == null)
                return;
            children.remove(child.getName());
        }
        child.setParent(null);

    }
}