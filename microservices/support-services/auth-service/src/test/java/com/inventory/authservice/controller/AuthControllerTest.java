package com.inventory.authservice.controller;

import com.inventory.authservice.dto.LoginResponse;
import com.inventory.authservice.entity.User;
import com.inventory.authservice.repository.IUserRepository;
import com.inventory.authservice.security.PasswordPolicyValidator;
import com.inventory.authservice.service.AccountLockoutService;
import com.inventory.authservice.service.DynamicPermissionService;
import com.inventory.authservice.service.JwtBlacklistService;
import com.inventory.authservice.service.MFAService;
import com.inventory.authservice.service.SecurityAuditService;
import com.inventory.authservice.service.TokenService;
import com.inventory.common.core.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private MFAService mfaService;
    @Mock
    private AccountLockoutService accountLockoutService;
    @Mock
    private JwtBlacklistService jwtBlacklistService;
    @Mock
    private SecurityAuditService securityAuditService;
    @Mock
    private DynamicPermissionService permissionService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordPolicyValidator passwordPolicyValidator;

    @InjectMocks
    private AuthController authController;

    private HttpServletRequest httpRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        httpRequest = mock(HttpServletRequest.class);
        lenient().when(httpRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        lenient().when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("alice");
        testUser.setEmail("alice@example.com");
        testUser.setPassword("encoded");
        testUser.setEnabled(true);
        testUser.setMfaEnabled(false);
    }

    @Test
    void testLoginSuccess() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("alice");
        req.setPassword("pwd123");

        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pwd123");
        when(accountLockoutService.isLocked("alice")).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(permissionService.getUserRoles("alice")).thenReturn(Set.of("USER"));
        when(tokenService.generateAccessToken(eq("alice"), any())).thenReturn("access-tok");
        when(tokenService.generateRefreshToken("alice")).thenReturn("refresh-tok");
        doNothing().when(jwtBlacklistService).registerUserToken(anyString(), anyString());
        doNothing().when(securityAuditService).logLoginSuccess(anyString(), anyString(), anyString());

        ResponseEntity<ApiResponse<LoginResponse>> response = authController.login(req, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access-tok", response.getBody().getData().getAccessToken());
        assertEquals("Bearer", response.getBody().getData().getTokenType());
    }

    @Test
    void testLoginAccountLocked() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("locked");
        req.setPassword("pwd");

        when(accountLockoutService.isLocked("locked")).thenReturn(true);
        when(accountLockoutService.getRemainingLockTime("locked")).thenReturn(5L);

        ResponseEntity<ApiResponse<LoginResponse>> response = authController.login(req, httpRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCOUNT_LOCKED", response.getBody().getErrorCode());
    }

    @Test
    void testLoginBadCredentials() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("alice");
        req.setPassword("wrong");

        when(accountLockoutService.isLocked("alice")).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad"));
        doNothing().when(accountLockoutService).recordFailedAttempt(anyString(), anyString());
        when(accountLockoutService.getRemainingAttempts("alice")).thenReturn(2);

        ResponseEntity<ApiResponse<LoginResponse>> response = authController.login(req, httpRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", response.getBody().getErrorCode());
    }

    @Test
    void testRefreshToken() {
        AuthController.RefreshTokenRequest req = new AuthController.RefreshTokenRequest();
        req.setRefreshToken("rtok");

        when(jwtBlacklistService.isBlacklisted("rtok")).thenReturn(false);
        when(tokenService.refreshToken("rtok")).thenReturn("new-atok");
        when(tokenService.getUsernameFromToken("rtok")).thenReturn("alice");
        doNothing().when(jwtBlacklistService).registerUserToken(anyString(), anyString());

        ResponseEntity<ApiResponse<java.util.Map<String, String>>> response =
                authController.refreshToken(req, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData().get("accessToken"));
    }

    @Test
    void testRefreshTokenRevoked() {
        AuthController.RefreshTokenRequest req = new AuthController.RefreshTokenRequest();
        req.setRefreshToken("rtok");

        when(jwtBlacklistService.isBlacklisted("rtok")).thenReturn(true);

        ResponseEntity<ApiResponse<java.util.Map<String, String>>> response =
                authController.refreshToken(req, httpRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("TOKEN_REVOKED", response.getBody().getErrorCode());
    }

    @Test
    void testLogoutNoAuth() {
        ResponseEntity<ApiResponse<Void>> response = authController.logout(httpRequest, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testLogoutWithAuth() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pwd");
        lenient().when(httpRequest.getHeader("Authorization")).thenReturn("Bearer abc");
        doNothing().when(jwtBlacklistService).blacklistToken(anyString(), anyString());
        doNothing().when(securityAuditService).logLogout(anyString(), anyString(), anyString());

        ResponseEntity<ApiResponse<Void>> response = authController.logout(httpRequest, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testLogoutAll() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pwd");
        doNothing().when(jwtBlacklistService).blacklistAllUserTokens("alice");

        ResponseEntity<ApiResponse<Void>> response = authController.logoutAll(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testChangePasswordNoAuth() {
        AuthController.ChangePasswordRequest req = new AuthController.ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("newpass1");
        req.setConfirmPassword("newpass1");

        ResponseEntity<ApiResponse<Void>> response = authController.changePassword(req, null, httpRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testChangePasswordMismatch() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pwd");
        AuthController.ChangePasswordRequest req = new AuthController.ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("newpass1");
        req.setConfirmPassword("different");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("old", "encoded")).thenReturn(true);

        ResponseEntity<ApiResponse<Void>> response = authController.changePassword(req, auth, httpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("PASSWORD_MISMATCH", response.getBody().getErrorCode());
    }

    @Test
    void testChangePasswordWrongCurrent() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pwd");
        AuthController.ChangePasswordRequest req = new AuthController.ChangePasswordRequest();
        req.setCurrentPassword("wrong");
        req.setNewPassword("newpass1");
        req.setConfirmPassword("newpass1");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        ResponseEntity<ApiResponse<Void>> response = authController.changePassword(req, auth, httpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_CURRENT_PASSWORD", response.getBody().getErrorCode());
    }

    @Test
    void testForgotPassword() {
        AuthController.ForgotPasswordRequest req = new AuthController.ForgotPasswordRequest();
        req.setEmail("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));

        ResponseEntity<ApiResponse<Void>> response = authController.forgotPassword(req, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetCurrentUserNoAuth() {
        ResponseEntity<ApiResponse<java.util.Map<String, Object>>> response =
                authController.getCurrentUser(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetCurrentUserWithAuth() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pwd");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        when(permissionService.getUserRoles("alice")).thenReturn(Set.of("USER"));
        when(permissionService.getUserPermissions("alice")).thenReturn(Set.of("read"));

        ResponseEntity<ApiResponse<java.util.Map<String, Object>>> response =
                authController.getCurrentUser(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("alice", response.getBody().getData().get("username"));
    }

    @Test
    void testGetUserPermissions() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pwd");
        when(permissionService.getUserPermissions("alice")).thenReturn(Set.of("read", "write"));

        ResponseEntity<ApiResponse<Set<String>>> response = authController.getUserPermissions(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().contains("read"));
    }

    @Test
    void testGetUserRoles() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pwd");
        when(permissionService.getUserRoles("alice")).thenReturn(Set.of("ADMIN"));

        ResponseEntity<ApiResponse<Set<String>>> response = authController.getUserRoles(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().contains("ADMIN"));
    }
}
