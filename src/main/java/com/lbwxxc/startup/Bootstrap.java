package com.lbwxxc.startup;


import com.lbwxxc.connect.http.HttpConnector;
import com.lbwxxc.core.StandardContext;
import com.lbwxxc.log.FileLogger;

import java.io.File;

/**
 * @author lbwxxc
 * @date 2025/8/13 17:21
 * @description:
 */
public class Bootstrap {

    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";

    public static void main(String[] args) {
        FileLogger fileLogger = new FileLogger();
        HttpConnector httpConnector = new HttpConnector();
        StandardContext standardContext = new StandardContext();
        httpConnector.setContainer(standardContext);
        standardContext.setConnector(httpConnector);
        standardContext.setLogger(fileLogger);
        httpConnector.start();
    }

}
