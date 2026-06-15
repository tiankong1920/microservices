package com.inventory.gatewayservice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String JWT_SECRET = "mySecretKeyForJWT2024TestingPurposeOnly";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", JWT_SECRET);
    }

    private String generateValidToken() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(TEST_USERNAME)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    private String generateExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(TEST_USERNAME)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    void testValidateTokenWithValidToken() {
        String token = generateValidToken();

        boolean result = jwtUtil.validateToken(token);

        assertTrue(result);
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        String invalidToken = "invalid.token.string";

        boolean result = jwtUtil.validateToken(invalidToken);

        assertFalse(result);
    }

    @Test
    void testValidateTokenWithExpiredToken() {
        String token = generateExpiredToken();

        boolean result = jwtUtil.validateToken(token);

        assertFalse(result);
    }

    @Test
    void testGetUsernameFromToken() {
        String token = generateValidToken();

        String username = jwtUtil.getUsernameFromToken(token);

        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void testGetUsernameFromTokenWithInvalidToken() {
        String invalidToken = "invalid.token.string";

        String username = jwtUtil.getUsernameFromToken(invalidToken);

        assertNull(username);
    }

    @Test
    void testIsTokenExpiredWithValidToken() {
        String token = generateValidToken();

        boolean result = jwtUtil.isTokenExpired(token);

        assertFalse(result);
    }

    @Test
    void testIsTokenExpiredWithExpiredToken() {
        String token = generateExpiredToken();

        boolean result = jwtUtil.isTokenExpired(token);

        assertTrue(result);
    }

    @Test
    void testIsTokenExpiredWithInvalidToken() {
        String invalidToken = "invalid.token.string";

        boolean result = jwtUtil.isTokenExpired(invalidToken);

        assertTrue(result);
    }

    @Test
    void testValidateTokenWithNullToken() {
        boolean result = jwtUtil.validateToken(null);

        assertFalse(result);
    }

    @Test
    void testGetUsernameFromTokenWithNullToken() {
        String username = jwtUtil.getUsernameFromToken(null);

        assertNull(username);
    }

    private String generateTokenWithRoles(List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(TEST_USERNAME)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    private String generateTokenWithoutRoles() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(TEST_USERNAME)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    void testGetRolesFromTokenWithRoles() {
        List<String> expectedRoles = List.of("ROLE_USER", "ROLE_ADMIN");
        String token = generateTokenWithRoles(expectedRoles);

        List<String> roles = jwtUtil.getRolesFromToken(token);

        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void testGetRolesFromTokenWithoutRoles() {
        String token = generateTokenWithoutRoles();

        List<String> roles = jwtUtil.getRolesFromToken(token);

        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test
    void testGetRolesFromTokenWithNullToken() {
        List<String> roles = jwtUtil.getRolesFromToken(null);

        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test
    void testGetRolesFromTokenWithInvalidToken() {
        List<String> roles = jwtUtil.getRolesFromToken("invalid.token.string");

        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test
    void testGetRolesFromClaimsWithRoles() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(TEST_USERNAME)
                .claim("roles", List.of("ROLE_MANAGER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> roles = jwtUtil.getRolesFromClaims(claims);

        assertEquals(1, roles.size());
        assertEquals("ROLE_MANAGER", roles.get(0));
    }

    @Test
    void testGetRolesFromClaimsWithNull() {
        List<String> roles = jwtUtil.getRolesFromClaims(null);

        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }
}
