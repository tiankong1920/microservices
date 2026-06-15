package com.inventory.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class JwtBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    private JwtBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        blacklistService = new JwtBlacklistService(redisTemplate);
    }

    @Test
    @DisplayName("Blacklist token should store in Redis")
    void blacklistToken_shouldStoreInRedis() {
        String token = "test.jwt.token";
        String reason = "User logout";

        blacklistService.blacklistToken(token, reason);

        verify(valueOperations).set(eq("jwt:blacklist:" + token), eq(reason), any(Duration.class));
    }

    @Test
    @DisplayName("Is blacklisted should check Redis")
    void testIsBlacklistedShouldCheckRedis() {
        String token = "test.jwt.token";

        when(redisTemplate.hasKey("jwt:blacklist:" + token)).thenReturn(true);

        assertTrue(blacklistService.isBlacklisted(token));

        verify(redisTemplate).hasKey("jwt:blacklist:" + token);
    }

    @Test
    @DisplayName("Non-blacklisted token should return false")
    void nonBlacklistedToken_shouldReturnFalse() {
        String token = "test.jwt.token";

        when(redisTemplate.hasKey("jwt:blacklist:" + token)).thenReturn(false);

        assertFalse(blacklistService.isBlacklisted(token));
    }

    @Test
    @DisplayName("Register user token should add to set")
    void testRegisterUserTokenShouldAddToSet() {
        String username = "testuser";
        String token = "test.jwt.token";

        blacklistService.registerUserToken(username, token);

        verify(setOperations).add("jwt:user:tokens:" + username, token);
    }

    @Test
    @DisplayName("Blacklist all user tokens should blacklist each token")
    void blacklistAllUserTokens_shouldBlacklistEachToken() {
        String username = "testuser";
        Set<String> tokens = new HashSet<>();
        tokens.add("token1");
        tokens.add("token2");

        when(setOperations.members("jwt:user:tokens:" + username)).thenReturn(tokens);

        blacklistService.blacklistAllUserTokens(username);

        verify(valueOperations, times(2)).set(anyString(), anyString(), any(Duration.class));
        verify(redisTemplate).delete("jwt:user:tokens:" + username);
    }

    @Test
    @DisplayName("Remove token from whitelist should remove from set")
    void testRemoveTokenFromWhitelistShouldRemoveFromSet() {
        String username = "testuser";
        String token = "test.jwt.token";

        blacklistService.removeTokenFromWhitelist(username, token);

        verify(setOperations).remove("jwt:user:tokens:" + username, token);
    }
}
