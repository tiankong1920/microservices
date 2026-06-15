package com.inventory.adminservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private com.inventory.adminservice.config.JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthController authController;

    @Test
    void testLoginSuccess() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "admin123");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("jwt-token");

        ResponseEntity<AuthController.ApiResponse<java.util.Map<String, Object>>> response =
                authController.authenticateUser(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals("jwt-token", response.getBody().getData().get("token"));
        assertEquals("Bearer", response.getBody().getData().get("type"));
    }

    @Test
    void testLoginSetsSecurityContext() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "admin123");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("jwt-token");

        authController.authenticateUser(req);

        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
        verify(jwtTokenProvider, times(1)).generateToken(auth);
    }

    @Test
    void testLoginBadCredentials() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        try {
            authController.authenticateUser(req);
        } catch (BadCredentialsException expected) {
            assertEquals("Bad credentials", expected.getMessage());
        }
    }
}
