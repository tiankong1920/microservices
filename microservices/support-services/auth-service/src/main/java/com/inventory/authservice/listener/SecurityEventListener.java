package com.inventory.authservice.listener;

import com.inventory.authservice.service.SecurityAuditService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

    private final SecurityAuditService securityAuditService;

    public SecurityEventListener(SecurityAuditService securityAuditService) {
        this.securityAuditService = securityAuditService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        String username = authentication.getName();
        String ipAddress = extractIpAddress(authentication);
        String userAgent = extractUserAgent(authentication);
        
        securityAuditService.logLoginSuccess(username, ipAddress, userAgent);
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        Authentication authentication = event.getAuthentication();
        String username = authentication.getName();
        String ipAddress = extractIpAddress(authentication);
        String reason = event.getException().getMessage();
        
        securityAuditService.logLoginFailure(username, ipAddress, reason);
    }

    private String extractIpAddress(Authentication authentication) {
        if (authentication.getDetails() instanceof WebAuthenticationDetails details) {
            return details.getRemoteAddress();
        }
        return "unknown";
    }

    @SuppressWarnings("unused")
    private String extractUserAgent(Authentication authentication) {
        return "unknown";
    }
}
