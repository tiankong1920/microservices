package com.inventory.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountLockoutServiceTest {

    private AccountLockoutService lockoutService;

    @BeforeEach
    void setUp() {
        lockoutService = new AccountLockoutService();
    }

    @Test
    @DisplayName("Record failed attempt should increment counter")
    void testRecordFailedAttemptShouldIncrementCounter() {
        String username = "testuser";
        String ipAddress = "192.168.1.1";
        
        assertEquals(5, lockoutService.getRemainingAttempts(username));
        
        lockoutService.recordFailedAttempt(username, ipAddress);
        
        assertEquals(4, lockoutService.getRemainingAttempts(username));
    }

    @Test
    @DisplayName("Reset failed attempts should clear counter")
    void testResetFailedAttemptsShouldClearCounter() {
        String username = "testuser";
        
        lockoutService.recordFailedAttempt(username, "192.168.1.1");
        lockoutService.recordFailedAttempt(username, "192.168.1.1");
        
        assertEquals(3, lockoutService.getRemainingAttempts(username));
        
        lockoutService.resetFailedAttempts(username);
        
        assertEquals(5, lockoutService.getRemainingAttempts(username));
    }

    @Test
    @DisplayName("Is locked should return false initially")
    void testIsLockedShouldReturnFalseInitially() {
        String username = "testuser";
        
        assertFalse(lockoutService.isLocked(username));
    }

    @Test
    @DisplayName("Account should be locked after max failed attempts")
    void accountShouldBeLockedAfterMaxFailedAttempts() {
        String username = "testuser";
        
        for (int i = 0; i < 5; i++) {
            lockoutService.recordFailedAttempt(username, "192.168.1.1");
        }
        
        assertTrue(lockoutService.isLocked(username));
    }

    @Test
    @DisplayName("Get remaining attempts should return correct count")
    void testGetRemainingAttemptsShouldReturnCorrectCount() {
        String username = "testuser";
        
        assertEquals(5, lockoutService.getRemainingAttempts(username));
        
        lockoutService.recordFailedAttempt(username, "192.168.1.1");
        
        assertEquals(4, lockoutService.getRemainingAttempts(username));
    }

    @Test
    @DisplayName("Get remaining lock time should return 0 when not locked")
    void testGetRemainingLockTimeShouldReturnZeroWhenNotLocked() {
        String username = "testuser";
        
        assertEquals(0, lockoutService.getRemainingLockTime(username));
    }
}
