package com.inventory.authservice.security;

import com.inventory.authservice.service.JwtBlacklistService;
import com.inventory.authservice.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsernamePasswordAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (tokenService.validateToken(token) && !jwtBlacklistService.isBlacklisted(token)) {
                String username = tokenService.getUsernameFromToken(token);
                List<SimpleGrantedAuthority> authorities = extractAuthoritiesFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set authentication for user: {}", username);
            } else {
                log.warn("Invalid or blacklisted JWT token detected");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> extractAuthoritiesFromToken(String token) {
        Claims claims = tokenService.extractAllClaims(token);
        List<String> roles = claims.get("roles", List.class);
        if (roles == null || roles.isEmpty()) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? new SimpleGrantedAuthority(role)
                        : new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/api/v1/auth/refresh") ||
               path.startsWith("/api/v1/auth/forgot-password") ||
               path.startsWith("/api/v1/auth/reset-password") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}
