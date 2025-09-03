package com.lbwxxc.valves;


import com.lbwxxc.Container;
import com.lbwxxc.Valve;

public abstract class ValveBase implements Valve {

    protected Container container = null;
    protected int debug = 0;
    protected static String info = "com.lbwxxc.valves.ValveBase/0.1";

    public Container getContainer() {
        return (container);
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public int getDebug() {
        return (this.debug);
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

    public String getInfo() {
        return (info);
    }
}