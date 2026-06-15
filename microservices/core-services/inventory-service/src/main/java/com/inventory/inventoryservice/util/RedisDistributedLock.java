package com.inventory.inventoryservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based distributed lock implementation.
 * Provides simple distributed locking capabilities using Redis.
 */
@Component
@RequiredArgsConstructor
public class RedisDistributedLock {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(RedisDistributedLock.class);

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_EXPIRE_TIME = 30;

    /**
 * Attempts to acquire a distributed lock with the specified expiration time.
     *
 * @param key the lock key (without prefix)
 * @param expireTime the lock expiration time in seconds
 * @return true if the lock was acquired successfully, false otherwise
     */
    public boolean tryLock(String key, long expireTime) {
        try {
            final String lockKey = LOCK_PREFIX + key;
            final Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", expireTime, TimeUnit.SECONDS);
            LOG.debug("Try to acquire lock: {}, result: {}", lockKey, locked);
            return Boolean.TRUE.equals(locked);
        } catch (RedisConnectionFailureException e) {
            LOG.error("Failed to acquire lock: {}", key, e);
            return false;
        }
    }

    /**
 * Attempts to acquire a distributed lock with the default expiration time.
     *
 * @param key the lock key (without prefix)
 * @return true if the lock was acquired successfully, false otherwise
     */
    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_EXPIRE_TIME);
    }

    /**
 * Releases the distributed lock.
     *
 * @param key the lock key (without prefix)
     */
    public void unlock(String key) {
        try {
            final String lockKey = LOCK_PREFIX + key;
            redisTemplate.delete(lockKey);
            LOG.debug("Released lock: {}", lockKey);
        } catch (RedisConnectionFailureException e) {
            LOG.error("Failed to release lock: {}", key, e);
        }
    }
}
