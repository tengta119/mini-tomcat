package com.lbwxxc.test;


import javax.servlet.*;
import java.io.IOException;

/**
 * @author lbwxxc
 * @date 2025/9/3 19:49
 * @description: TODO
 */
public class TestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("The very first Filter");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
