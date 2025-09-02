package com.lbwxxc.log;

public class SystemErrLogger extends LoggerBase {
    protected static final String info =
            "com.lbwxxc.logger.SystemErrLogger/0.1";


    public void log(String msg) {
        System.err.println(msg);
    }


}