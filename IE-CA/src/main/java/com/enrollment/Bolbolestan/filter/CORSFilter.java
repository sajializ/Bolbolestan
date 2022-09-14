package com.enrollment.Bolbolestan.filter;

import org.springframework.core.annotation.Order;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CORSFilter implements Filter {
    public CORSFilter() {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods","GET, PUT, POST, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers"," Origin, Content-Type, X-Auth-Token, authorization");
        if (request.getMethod().equals("OPTIONS")) {
            String reqOrigin = request.getHeader("Origin");
            response.addHeader("Access-Control-Max-Age", "1728000");
            response.addHeader("Access-Control-Allow-Headers","*");
            response.addHeader("Access-Control-Allow-Origin",reqOrigin);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(request, servletResponse);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

}