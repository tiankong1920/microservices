package com.inventory.authservice.controller;

import com.inventory.authservice.dto.LoginResponse;
import com.inventory.common.core.ApiResponse;
import com.inventory.common.core.MessageConstants;
import com.inventory.authservice.entity.User;
import com.inventory.authservice.repository.IUserRepository;
import com.inventory.authservice.security.PasswordPolicyValidator;
import com.inventory.authservice.service.AccountLockoutService;
import com.inventory.authservice.service.DynamicPermissionService;
import com.inventory.authservice.service.JwtBlacklistService;
import com.inventory.authservice.service.MFAService;
import com.inventory.authservice.service.SecurityAuditService;
import com.inventory.authservice.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 认证控制器 - 处理用户认证、授权、令牌管理等功能
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final IUserRepository userRepository;
    private final MFAService mfaService;
    private final AccountLockoutService accountLockoutService;
    private final JwtBlacklistService jwtBlacklistService;
    private final SecurityAuditService securityAuditService;
    private final DynamicPermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        private Integer mfaCode;
    }

    @Data
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        private String newPassword;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;
    }

    @Data
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "Token is required")
        private String token;

        @NotBlank(message = "New password is required")
        private String newPassword;
    }

    /**
     * 用户登录认证
     *
     * @param loginRequest 包含username、password和可选mfaCode的登录请求
     * @param request HTTP请求对象，用于获取客户端IP和User-Agent
     * @return 登录响应，包含访问令牌和刷新令牌
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        String username = loginRequest.getUsername();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        try {
            if (accountLockoutService.isLocked(username)) {
                long remainingMinutes = accountLockoutService.getRemainingLockTime(username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                        "Account is locked. Please try again in " + remainingMinutes + " minutes.",
                        "ACCOUNT_LOCKED"
                    ));
            }

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

            if (user.getMfaEnabled()) {
                if (loginRequest.getMfaCode() == null) {
                    String mfaSessionId = mfaService.createMfaSession(username);
                    LoginResponse mfaResponse = LoginResponse.builder()
                        .mfaRequired(true)
                        .mfaSessionId(mfaSessionId)
                        .build();

                    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .body(ApiResponse.success("MFA verification required", mfaResponse));
                }

                boolean mfaValid = mfaService.verifyMFACode(user, loginRequest.getMfaCode());
                if (!mfaValid) {
                    securityAuditService.logMfaEvent(username, "MFA_FAILURE", ipAddress, "Invalid MFA code");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid MFA code", "INVALID_MFA_CODE"));
                }
                securityAuditService.logMfaEvent(username, "MFA_SUCCESS", ipAddress, "MFA verification successful");
            }

            accountLockoutService.resetFailedAttempts(username);

            Set<String> roles = permissionService.getUserRoles(username);

            Map<String, Object> claims = new HashMap<>();
            claims.put("username", user.getUsername());
            claims.put("email", user.getEmail());
            claims.put("mfaEnabled", user.getMfaEnabled());
            claims.put("roles", roles);

            String accessToken = tokenService.generateAccessToken(username, claims);
            String refreshToken = tokenService.generateRefreshToken(username);

            jwtBlacklistService.registerUserToken(username, accessToken);

            LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .username(user.getUsername())
                .roles(List.copyOf(roles))
                .build();

            securityAuditService.logLoginSuccess(username, ipAddress, userAgent);

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Account is locked", "ACCOUNT_LOCKED"));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Account is disabled", "ACCOUNT_DISABLED"));
        } catch (BadCredentialsException e) {
            accountLockoutService.recordFailedAttempt(username, ipAddress);
            int remaining = accountLockoutService.getRemainingAttempts(username);

            String message = remaining > 0
                ? "Invalid username or password. " + remaining + " attempts remaining."
                : "Account has been locked due to too many failed attempts.";

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message, "INVALID_CREDENTIALS"));
        } catch (Exception e) {
            securityAuditService.logSecurityEvent("LOGIN_ERROR", username, ipAddress, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Authentication failed: " + e.getMessage(), "AUTH_ERROR"));
        }
    }

    /**
     * 刷新访问令牌
     *
     * @param request 包含refreshToken的请求数据
     * @param httpRequest HTTP请求对象
     * @return 新的访问令牌信息
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        try {
            String refreshToken = request.getRefreshToken();

            if (jwtBlacklistService.isBlacklisted(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token has been revoked", "TOKEN_REVOKED"));
            }

            String newAccessToken = tokenService.refreshToken(refreshToken);
            String username = tokenService.getUsernameFromToken(refreshToken);
            String ipAddress = getClientIpAddress(httpRequest);

            jwtBlacklistService.registerUserToken(username, newAccessToken);
            securityAuditService.logSecurityEvent("TOKEN_REFRESH", username, ipAddress, "Token refreshed");

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("tokenType", "Bearer");

            return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid refresh token", "INVALID_REFRESH_TOKEN"));
        }
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求对象
     * @param authentication 当前用户认证信息
     * @return 登出成功响应
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            Authentication authentication) {

        if (authentication != null) {
            String username = authentication.getName();
            String ipAddress = getClientIpAddress(request);

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                jwtBlacklistService.blacklistToken(token, "User logout");
            }

            securityAuditService.logLogout(username, ipAddress, "Successful logout");
        }

        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * 登出所有设备
     *
     * @param authentication 当前用户认证信息
     * @return 所有会话终止成功响应
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            jwtBlacklistService.blacklistAllUserTokens(username);
            securityAuditService.logSecurityEvent("LOGOUT_ALL", username, "system", "All sessions terminated");
        }

        return ResponseEntity.ok(ApiResponse.success("All sessions terminated", null));
    }

    /**
     * 修改密码
     *
     * @param request 包含currentPassword、newPassword和confirmPassword的请求数据
     * @param authentication 当前用户认证信息
     * @param httpRequest HTTP请求对象
     * @return 密码修改成功响应
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(MessageConstants.NOT_AUTHENTICATED, "NOT_AUTHENTICATED"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Current password is incorrect", "INVALID_CURRENT_PASSWORD"));
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Passwords do not match", "PASSWORD_MISMATCH"));
        }

        PasswordPolicyValidator.PasswordValidationResult validationResult =
            passwordPolicyValidator.validate(request.getNewPassword());

        if (!validationResult.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(validationResult.getViolationMessage(), "PASSWORD_POLICY_VIOLATION"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        jwtBlacklistService.blacklistAllUserTokens(username);
        securityAuditService.logPasswordChange(username, getClientIpAddress(httpRequest));

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    /**
     * 忘记密码请求
     *
     * @param request 包含email的请求数据
     * @param httpRequest HTTP请求对象
     * @return 密码重置邮件发送响应
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        String email = request.getEmail();
        String ipAddress = getClientIpAddress(httpRequest);

        userRepository.findByEmail(email).ifPresent(user -> {
            securityAuditService.logSecurityEvent(
                "PASSWORD_RESET_REQUEST",
                user.getUsername(),
                ipAddress,
                "Password reset requested"
            );
        });

        return ResponseEntity.ok(ApiResponse.success(
            "If the email exists, a password reset link has been sent",
            null
        ));
    }

    /**
     * 获取当前登录用户信息
     *
     * @param authentication 当前用户认证信息
     * @return 当前用户信息，包括用户名、邮箱、角色和权限
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(MessageConstants.NOT_AUTHENTICATED, "NOT_AUTHENTICATED"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

        Set<String> roles = permissionService.getUserRoles(username);
        Set<String> permissions = permissionService.getUserPermissions(username);

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("enabled", user.getEnabled());
        response.put("mfaEnabled", user.getMfaEnabled());
        response.put("roles", roles);
        response.put("permissions", permissions);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取当前用户权限
     *
     * @param authentication 当前用户认证信息
     * @return 用户权限集合
     */
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<Set<String>>> getUserPermissions(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(MessageConstants.NOT_AUTHENTICATED, "NOT_AUTHENTICATED"));
        }

        Set<String> permissions = permissionService.getUserPermissions(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * 获取当前用户角色
     *
     * @param authentication 当前用户认证信息
     * @return 用户角色集合
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<Set<String>>> getUserRoles(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(MessageConstants.NOT_AUTHENTICATED, "NOT_AUTHENTICATED"));
        }

        Set<String> roles = permissionService.getUserRoles(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
