package com.inventory.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class AccountLockoutService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    private final Map<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockoutTimes = new ConcurrentHashMap<>();

    public boolean isLocked(String username) {
        LocalDateTime lockoutTime = lockoutTimes.get(username);
        if (lockoutTime == null) {
            return false;
        }

        if (lockoutTime.plusMinutes(LOCKOUT_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
            resetFailedAttempts(username);
            return false;
        }

        return true;
    }

    public void recordFailedAttempt(String username, String ipAddress) {
        AtomicInteger attempts = failedAttempts.computeIfAbsent(username, k -> new AtomicInteger(0));
        int count = attempts.incrementAndGet();

        log.warn("Failed login attempt {} for user: {} from IP: {}", count, username, ipAddress);

        if (count >= MAX_FAILED_ATTEMPTS) {
            lockAccount(username);
        }
    }

    public void resetFailedAttempts(String username) {
        failedAttempts.remove(username);
        lockoutTimes.remove(username);
        log.info("Reset failed attempts for user: {}", username);
    }

    private void lockAccount(String username) {
        lockoutTimes.put(username, LocalDateTime.now());
        log.warn("Account locked for user: {} until {}", username, 
                LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
    }

    public int getRemainingAttempts(String username) {
        AtomicInteger attempts = failedAttempts.get(username);
        if (attempts == null) {
            return MAX_FAILED_ATTEMPTS;
        }
        return Math.max(0, MAX_FAILED_ATTEMPTS - attempts.get());
    }

    public long getRemainingLockTime(String username) {
        LocalDateTime lockoutTime = lockoutTimes.get(username);
        if (lockoutTime == null) {
            return 0;
        }
        LocalDateTime endTime = lockoutTime.plusMinutes(LOCKOUT_DURATION_MINUTES);
        if (endTime.isBefore(LocalDateTime.now())) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), endTime).toMinutes();
    }

    public LocalDateTime getLockoutEndTime(String username) {
        LocalDateTime lockoutTime = lockoutTimes.get(username);
        if (lockoutTime == null) {
            return null;
        }
        return lockoutTime.plusMinutes(LOCKOUT_DURATION_MINUTES);
    }
}
