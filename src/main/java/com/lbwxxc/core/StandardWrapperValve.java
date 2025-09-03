package com.lbwxxc.core;



import com.lbwxxc.Request;
import com.lbwxxc.Response;
import com.lbwxxc.ValveContext;
import com.lbwxxc.connect.HttpRequestFacade;
import com.lbwxxc.connect.HttpResponseFacade;
import com.lbwxxc.connect.http.HttpRequestImpl;
import com.lbwxxc.connect.http.HttpResponseImpl;
import com.lbwxxc.valves.ValveBase;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StandardWrapperValve extends ValveBase {

    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        // TODO Auto-generated method stub
        System.out.println("StandardWrapperValve invoke()");

        HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
        HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);

        Servlet instance = ((StandardWrapper)getContainer()).getServlet();
        if (instance != null) {
            instance.service(requestFacade, responseFacade);
        }
    }
}
