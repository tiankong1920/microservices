package com.inventory.authservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@SuppressWarnings("null")
public class JwtBlacklistService {

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String USER_TOKENS_PREFIX = "jwt:user:tokens:";
    private static final Duration DEFAULT_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public JwtBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, String reason) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, reason, DEFAULT_TTL);
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void blacklistAllUserTokens(String username) {
        String userTokensKey = USER_TOKENS_PREFIX + username;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
        
        if (tokens != null) {
            for (String token : tokens) {
                blacklistToken(token, "User initiated logout all");
            }
        }
        
        redisTemplate.delete(userTokensKey);
    }

    public void registerUserToken(String username, String token) {
        String userTokensKey = USER_TOKENS_PREFIX + username;
        redisTemplate.opsForSet().add(userTokensKey, token);
        redisTemplate.expire(userTokensKey, DEFAULT_TTL);
    }

    public void removeTokenFromWhitelist(String username, String token) {
        String userTokensKey = USER_TOKENS_PREFIX + username;
        redisTemplate.opsForSet().remove(userTokensKey, token);
    }

    public long getBlacklistSize() {
        Set<String> keys = redisTemplate.keys(BLACKLIST_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }

    public void cleanupExpiredTokens() {
        Set<String> keys = redisTemplate.keys(BLACKLIST_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                Long ttl = redisTemplate.getExpire(key);
                if (ttl != null && ttl < 0) {
                    redisTemplate.delete(key);
                }
            }
        }
    }
}
