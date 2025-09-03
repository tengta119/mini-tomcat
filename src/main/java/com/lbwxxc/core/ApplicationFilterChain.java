package com.lbwxxc.core;


import com.lbwxxc.connect.HttpRequestFacade;
import com.lbwxxc.connect.HttpResponseFacade;
import com.lbwxxc.connect.http.HttpRequestImpl;
import com.lbwxxc.connect.http.HttpResponseImpl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

final class ApplicationFilterChain implements FilterChain {

    public ApplicationFilterChain() {
        super();
    }
    private ArrayList<ApplicationFilterConfig> filters = new ArrayList<>();

    private Iterator<ApplicationFilterConfig> iterator = null;

    private Servlet servlet = null;

    public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        System.out.println("FilterChain doFilter()");
        internalDoFilter(request,response);
    }

    private void internalDoFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        // Construct an iterator the first time this method is called
        if (this.iterator == null)
            this.iterator = filters.iterator();

        // Call the next filter if there is one
        if (this.iterator.hasNext()) {
            ApplicationFilterConfig filterConfig =
                    (ApplicationFilterConfig) iterator.next();
            Filter filter = null;
            try {
                filter = filterConfig.getFilter();
                System.out.println("Filter doFilter()");

                filter.doFilter(request, response, this);
            } catch (IOException | ServletException | RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new ServletException("filterChain.filter", e);
            }
            return;
        }

        // We fell off the end of the chain -- call the servlet instance
        try {
            HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
            HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);

            servlet.service(requestFacade, responseFacade);
        } catch (IOException | ServletException | RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServletException("filterChain.servlet", e);
        }

    }

    void addFilter(ApplicationFilterConfig filterConfig) {
        this.filters.add(filterConfig);
    }

    void release() {
        this.filters.clear();
        this.iterator = iterator;
        this.servlet = null;
    }

    void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }
}