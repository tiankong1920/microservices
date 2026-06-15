package com.inventory.gatewayservice.service;

import com.inventory.gatewayservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;

    public boolean validateToken(String token) {
        try {
            boolean isValid = jwtUtil.validateToken(token);
            if (isValid) {
                String username = jwtUtil.getUsernameFromToken(token);
                log.debug("Token validated successfully for user: {}", username);
            }
            return isValid;
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            log.debug("Extracted username from token: {}", username);
            return username;
        } catch (JwtException e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            boolean isExpired = jwtUtil.isTokenExpired(token);
            if (isExpired) {
                log.debug("Token is expired");
            } else {
                log.debug("Token is not expired");
            }
            return isExpired;
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return true;
        }
    }

    public boolean hasValidAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("No valid authentication found");
            return false;
        }

        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) authentication;
            if (authToken.getPrincipal() == null) {
                log.debug("Authentication token principal is null");
                return false;
            }
        }

        log.debug("Valid authentication found for user: {}", authentication.getName());
        return true;
    }

    public List<String> getRolesFromToken(String token) {
        try {
            log.debug("Extracting roles from token");
            return jwtUtil.getRolesFromToken(token);
        } catch (JwtException e) {
            log.error("Failed to extract roles from token: {}", e.getMessage());
            return List.of();
        }
    }
}
