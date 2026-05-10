package com.blog.interceptor;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;

@WebFilter(urlPatterns = "/api/*")
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    static class XssRequestWrapper extends HttpServletRequestWrapper {
        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String[] getParameterValues(String parameter) {
            String[] values = super.getParameterValues(parameter);
            if (values == null) return null;
            String[] encodedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                encodedValues[i] = stripXss(values[i]);
            }
            return encodedValues;
        }

        @Override
        public String getParameter(String parameter) {
            String value = super.getParameter(parameter);
            return stripXss(value);
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return stripXss(value);
        }

        private String stripXss(String value) {
            if (value == null) return null;
            value = value.replaceAll("<script[^>]*?>.*?</script>", "")
                         .replaceAll("<iframe[^>]*?>.*?</iframe>", "")
                         .replaceAll("<object[^>]*?>.*?</object>", "")
                         .replaceAll("<embed[^>]*?>", "")
                         .replaceAll("javascript:", "")
                         .replaceAll("on\\w+\\s*=", "");
            return value;
        }
    }
}
