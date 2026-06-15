package com.inventory.gatewayservice.service;

import com.inventory.gatewayservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(jwtUtil);
    }

    @Test
    void testValidateTokenValid() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);

        assertTrue(authService.validateToken("valid-token"));
    }

    @Test
    void testValidateTokenInvalid() {
        when(jwtUtil.validateToken("invalid-token")).thenThrow(new io.jsonwebtoken.JwtException("Invalid"));

        assertFalse(authService.validateToken("invalid-token"));
    }

    @Test
    void testGetUsernameFromToken() {
        when(jwtUtil.getUsernameFromToken("valid-token")).thenReturn("testuser");

        String username = authService.getUsernameFromToken("valid-token");
        assertEquals("testuser", username);
    }

    @Test
    void testGetUsernameFromTokenFailure() {
        when(jwtUtil.getUsernameFromToken("bad-token")).thenThrow(new io.jsonwebtoken.JwtException("Invalid"));

        String username = authService.getUsernameFromToken("bad-token");
        assertEquals(null, username);
    }

    @Test
    void testIsTokenExpiredTrue() {
        when(jwtUtil.isTokenExpired("expired-token")).thenReturn(true);

        assertTrue(authService.isTokenExpired("expired-token"));
    }

    @Test
    void testIsTokenExpiredFalse() {
        when(jwtUtil.isTokenExpired("valid-token")).thenReturn(false);

        assertFalse(authService.isTokenExpired("valid-token"));
    }

    @Test
    void testGetRolesFromTokenValid() {
        List<String> expectedRoles = List.of("ROLE_USER", "ROLE_ADMIN");
        when(jwtUtil.getRolesFromToken("valid-token")).thenReturn(expectedRoles);

        List<String> roles = authService.getRolesFromToken("valid-token");
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
        assertEquals("ROLE_ADMIN", roles.get(1));
    }

    @Test
    void testGetRolesFromTokenInvalid() {
        when(jwtUtil.getRolesFromToken("invalid-token")).thenThrow(new io.jsonwebtoken.JwtException("Invalid"));

        List<String> roles = authService.getRolesFromToken("invalid-token");
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    void testGetRolesFromTokenEmptyRoles() {
        when(jwtUtil.getRolesFromToken("token-with-no-roles")).thenReturn(List.of("ROLE_USER"));

        List<String> roles = authService.getRolesFromToken("token-with-no-roles");
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }
}
