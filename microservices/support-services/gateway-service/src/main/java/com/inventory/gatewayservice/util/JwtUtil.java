package com.inventory.gatewayservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            log.error("Token validation failed: Token is null or empty");
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            log.error("Failed to extract username from token: Token is null or empty");
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiryDate = claims.getExpiration();
            return expiryDate.before(new Date());
        } catch (ExpiredJwtException e) {
            log.error("Token is expired: {}", e.getMessage());
            return true;
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromClaims(Claims claims) {
        if (claims == null) {
            log.error("Claims is null, cannot extract roles");
            return Collections.singletonList("ROLE_USER");
        }
        try {
            List<String> roles = claims.get("roles", List.class);
            if (roles == null || roles.isEmpty()) {
                log.debug("No roles found in token claims, defaulting to ROLE_USER");
                return Collections.singletonList("ROLE_USER");
            }
            log.debug("Extracted roles from token: {}", roles);
            return roles;
        } catch (Exception e) {
            log.error("Failed to extract roles from claims: {}", e.getMessage());
            return Collections.singletonList("ROLE_USER");
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        if (token == null || token.isEmpty()) {
            log.error("Failed to extract roles from token: Token is null or empty");
            return Collections.singletonList("ROLE_USER");
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return getRolesFromClaims(claims);
        } catch (JwtException e) {
            log.error("Failed to extract roles from token: {}", e.getMessage());
            return Collections.singletonList("ROLE_USER");
        }
    }
}
