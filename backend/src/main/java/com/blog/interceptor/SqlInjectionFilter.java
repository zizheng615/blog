package com.blog.interceptor;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;

@Slf4j
@WebFilter(urlPatterns = "/api/*")
public class SqlInjectionFilter implements Filter {

    private static final Pattern SQL_PATTERN = Pattern.compile(
        "(\\b(select|insert|update|delete|drop|truncate|union|exec|execute|script|eval|xp_cmdshell|declare|create|alter|grant|revoke)\\b)|(--|#|/\\*|\\*/|;|\\|\\||'\\s*or\\s*'|\"\\s*or\\s*\"|'\\s*and\\s*'|\"\\s*and\\s*\")",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (hasSqlInjection(httpRequest)) {
            log.warn("SQL injection attempt detected from IP: {}", request.getRemoteAddr());
            throw new ServletException("Invalid request parameters");
        }
        chain.doFilter(request, response);
    }

    private boolean hasSqlInjection(HttpServletRequest request) {
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            String[] values = request.getParameterValues(param);
            if (values != null) {
                for (String value : values) {
                    if (value != null && SQL_PATTERN.matcher(value).find()) {
                        log.warn("SQL injection pattern found in param '{}' value: {}", param, value);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
