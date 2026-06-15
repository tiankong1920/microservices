package com.inventory.authservice.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "jwtSecret", "thisIsAVeryLongSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong");
        ReflectionTestUtils.setField(tokenService, "jwtExpiration", 3600000L);
        ReflectionTestUtils.setField(tokenService, "jwtRefreshExpiration", 604800000L);
    }

    @Test
    @DisplayName("Generate access token should create valid JWT")
    void testGenerateAccessToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "USER");

        String token = tokenService.generateAccessToken("testuser", claims);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertEquals("testuser", tokenService.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("Generate refresh token should create valid JWT")
    void testGenerateRefreshToken() {
        String refreshToken = tokenService.generateRefreshToken("testuser");

        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
        assertEquals("testuser", tokenService.getUsernameFromToken(refreshToken));
    }

    @Test
    @DisplayName("Token should not be expired immediately after generation")
    void testTokenNotExpired() {
        String token = tokenService.generateAccessToken("testuser", new HashMap<>());

        assertFalse(tokenService.isTokenExpired(token));
    }

    @Test
    @DisplayName("Valid token should pass validation")
    void testValidateValidToken() {
        String token = tokenService.generateAccessToken("testuser", new HashMap<>());

        assertTrue(tokenService.validateToken(token));
    }

    @Test
    @DisplayName("Refresh token should be usable to get new access token")
    void testRefreshToken() {
        String refreshToken = tokenService.generateRefreshToken("testuser");

        String newAccessToken = tokenService.refreshToken(refreshToken);

        assertNotNull(newAccessToken);
        assertEquals("testuser", tokenService.getUsernameFromToken(newAccessToken));
    }

    @Test
    @DisplayName("Invalid refresh token should throw exception")
    void testRefreshWithInvalidToken() {
        assertThrows(IllegalStateException.class, () -> {
            tokenService.refreshToken("invalid.token.here");
        });
    }

    @Test
    @DisplayName("Extract all claims should return proper claims")
    void testExtractAllClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        claims.put("roles", "ADMIN");

        String token = tokenService.generateAccessToken("testuser", claims);
        Claims extractedClaims = tokenService.extractAllClaims(token);

        assertEquals("testuser", extractedClaims.getSubject());
        assertEquals("test@example.com", extractedClaims.get("email"));
    }

    @Test
    @DisplayName("Access token and refresh token should be different")
    void testAccessAndRefreshTokensAreDifferent() {
        String accessToken = tokenService.generateAccessToken("testuser", new HashMap<>());
        String refreshToken = tokenService.generateRefreshToken("testuser");

        assertNotEquals(accessToken, refreshToken);
    }
}
