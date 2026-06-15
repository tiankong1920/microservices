package com.inventory.authservice.security.handler;

import com.inventory.authservice.service.JwtBlacklistService;
import com.inventory.authservice.service.SecurityAuditService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private static final String ACCESS_TOKEN_COOKIE = "access-token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh-token";
    private static final String REMEMBER_ME_COOKIE = "remember-me";

    private final JwtBlacklistService jwtBlacklistService;
    private final SecurityAuditService securityAuditService;

    public CustomLogoutHandler(JwtBlacklistService jwtBlacklistService, SecurityAuditService securityAuditService) {
        this.jwtBlacklistService = jwtBlacklistService;
        this.securityAuditService = securityAuditService;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        
        String username = authentication != null ? authentication.getName() : "unknown";
        String ipAddress = getClientIpAddress(request);
        
        invalidateJwtTokens(request);
        
        invalidateSession(request);
        
        clearCookies(response);
        
        securityAuditService.logLogout(username, ipAddress, "Successful logout");
    }

    private void invalidateJwtTokens(HttpServletRequest request) {
        String accessToken = extractTokenFromCookie(request, ACCESS_TOKEN_COOKIE);
        String refreshToken = extractTokenFromCookie(request, REFRESH_TOKEN_COOKIE);
        
        if (accessToken != null) {
            jwtBlacklistService.blacklistToken(accessToken, "User logout");
        }
        
        if (refreshToken != null) {
            jwtBlacklistService.blacklistToken(refreshToken, "User logout");
        }
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private void clearCookies(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE);
        deleteCookie(response, REFRESH_TOKEN_COOKIE);
        deleteCookie(response, REMEMBER_ME_COOKIE);
        deleteCookie(response, "JSESSIONID");
    }

    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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
