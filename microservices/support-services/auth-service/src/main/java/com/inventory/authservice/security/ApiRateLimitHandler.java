package com.inventory.authservice.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * API限流拒绝处理器.
 * 当API请求被拒绝访问时返回403状态码.
 */
@Component
public class ApiRateLimitHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                   AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            "{\"error\":\"Access denied\",\"message\":\"Access to this resource is denied.\"}"
        );
    }
}
