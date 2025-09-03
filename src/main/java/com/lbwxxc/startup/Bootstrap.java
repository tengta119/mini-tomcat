package com.lbwxxc.startup;


import com.lbwxxc.Logger;
import com.lbwxxc.connect.http.HttpConnector;
import com.lbwxxc.core.FilterDef;
import com.lbwxxc.core.FilterMap;
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
    private static int debug = 1;
    public static void main(String[] args) {
        if (debug >= 1)
            log(".... startup ....");
        HttpConnector connector = new HttpConnector();
        StandardContext container = new StandardContext();
        connector.setContainer(container);
        container.setConnector(connector);

        Logger logger = new FileLogger();
        container.setLogger(logger);

        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("TestFilter");
        filterDef.setFilterClass("com.lbwxxc.test.TestFilter");
        container.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("TestFilter");
        filterMap.setURLPattern("/*");
        container.addFilterMap(filterMap);

        container.filterStart();
        connector.start();
    }
    private static void log(String message) {
        System.out.print("Bootstrap: ");
        System.out.println(message);
    }

    private static void log(String message, Throwable exception) {
        log(message);
        exception.printStackTrace(System.out);

    }

}
