package com.enrollment.Bolbolestan.filter;

import com.enrollment.Bolbolestan.utilities.JWT;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(1)
public class JWTFilter implements Filter {

    public JWTFilter() {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (request.getMethod().equals("OPTIONS")) {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Headers","Origin, Content-Type, X-Auth-Token, Authorization");
            response.setStatus(200);
            return;
        }
        if (!request.getRequestURI().startsWith("/students/me/") && !request.getRequestURI().startsWith("/courses")) {
            response.setStatus(200);
            chain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("authorization");
        if (authHeader == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access denied.");
            return;
        } else {
            String accessToken = authHeader.replace("\"", "");
            try {
                String id = JWT.decodeToken(accessToken).get("userId").asText();
                request.setAttribute("id", id);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("init() method has been get invoked");
        System.out.println("Filter name is "+ filterConfig.getFilterName());
        System.out.println("ServletContext name is"+ filterConfig.getServletContext());
        System.out.println("init() method is ended");
    }
}

@Configuration
class AppConfig {
    @Bean
    public FilterRegistrationBean<JWTFilter> authFilter(){
        FilterRegistrationBean<JWTFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new JWTFilter());
        registrationBean.addUrlPatterns("/students/me/*");
        registrationBean.addUrlPatterns("/courses/*");
        return registrationBean;
    }
}
