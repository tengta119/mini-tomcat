package com.lbwxxc.log;

public class SystemOutLogger extends LoggerBase {
    protected static final String info =
            "com.lbwxxc.logger.SystemOutLogger/1.0";

    public void log(String msg) {
        System.out.println(msg);
    }

}