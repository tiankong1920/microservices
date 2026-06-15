package com.inventory.authservice.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * API rate limit filter.
 */
@Component
public class ApiRateLimitFilter implements Filter {

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> windowStartTimes = new ConcurrentHashMap<>();
    
    private final boolean rateLimitEnabled;
    private final int requestsPerMinute;

    public ApiRateLimitFilter(boolean rateLimitEnabled, int requestsPerMinute) {
        this.rateLimitEnabled = rateLimitEnabled;
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if (!rateLimitEnabled) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String key = clientIp + ":" + getCurrentMinute();
        
        AtomicInteger count = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();

        // Check if rate limit exceeded
        if (currentCount > requestsPerMinute) {
            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                httpResponse.getWriter().write(
                    "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
            }
            return;
        }

        // Reset counter if new minute
        resetIfNewMinute(key);
        chain.doFilter(request, response);
    }

    /**
 * Get client IP address.
     */
    private String getClientIp(ServletRequest request) {
        if (request instanceof HttpServletRequest httpRequest) {
            String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = httpRequest.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return httpRequest.getRemoteAddr();
        }

        return "unknown";
    }

    /**
 * Get current minute identifier.
     */
    private String getCurrentMinute() {
        return LocalDateTime.now(ZoneId.of("UTC"))
                .truncatedTo(ChronoUnit.MINUTES)
                .toString();
    }

    /**
 * Reset counter if new minute.
     */
    private void resetIfNewMinute(String key) {
        String currentMinute = getCurrentMinute();
        windowStartTimes.computeIfPresent(key, (ignored, oldTime) -> {
            String oldTimeStr = oldTime.toString();
            if (!currentMinute.equals(oldTimeStr.substring(0, 16))) {
                requestCounts.remove(key);
            }
            return oldTime;
        });
    }
}
