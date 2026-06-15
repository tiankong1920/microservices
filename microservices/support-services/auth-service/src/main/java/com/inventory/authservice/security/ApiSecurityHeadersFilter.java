package com.inventory.authservice.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * API安全头过滤器.
 * 为所有API响应添加必要的安全头.
 */
@Component
public class ApiSecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        // 添加安全头
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
            httpResponse.setHeader(
                "Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self'; "
                + "img-src 'self' data: https:; font-src 'self'; frame-ancestors 'none';"
            );
        }
        
        // 添加CORS头（如果需要）
        String origin = null;
        if (request instanceof HttpServletRequest) {
            origin = ((HttpServletRequest) request).getHeader("Origin");
        }
        if (origin != null && (origin.contains("localhost") || origin.contains("127.0.0.1"))
                && response instanceof HttpServletResponse) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setHeader("Access-Control-Allow-Origin", origin);
                httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
                httpResponse.setHeader("Access-Control-Max-Age", "3600");
            }
        
        chain.doFilter(request, response);
    }
}
