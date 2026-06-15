package com.inventory.adminservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT (JSON Web Token) provider for authentication.
 * Handles token generation, validation, and user information extraction.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.in.ms}")
    private int jwtExpirationInMs;

    /**
 * Gets the signing key for JWT operations.
     *
     * @return the secret key for signing
     * @throws IllegalArgumentException if the secret key is invalid
     */
    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 256 bits (32 characters)");
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
 * Generates a JWT token for the authenticated user.
     *
     * @param authentication the authentication object containing user details
     * @return the generated JWT token
     */
    public String generateToken(Authentication authentication) {
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
 * Extracts the username from a JWT token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String getUsernameFromToken(String token) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
 * Validates a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
