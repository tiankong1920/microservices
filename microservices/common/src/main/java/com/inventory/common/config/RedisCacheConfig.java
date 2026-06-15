package com.inventory.common.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis缓存统一配置类，用于规范化缓存TTL和命名策略.
 * 确保所有模块使用相同的缓存配置和过期策略.
 */
@Configuration
@EnableCaching
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
@Slf4j
@SuppressWarnings("null")
public class RedisCacheConfig {

    /**
     * 配置Redis缓存管理器，设置统一的TTL策略.
     *
     * @param connectionFactory Redis连接工厂
     * @return Redis缓存管理器
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Configuring Redis cache manager with unified TTL strategy");

        final RedisCacheConfiguration config = redisCacheConfiguration();
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * Redis缓存默认配置，设置TTL和键命名规范.
     *
     * @return Redis缓存配置
     */
    private RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues();
    }

    /**
     * 产品专用缓存配置，设置较短TTL.
     *
     * @return 产品缓存配置
     */
    public RedisCacheConfiguration productCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues();
    }

    /**
     * 通用实体缓存配置，设置较长TTL.
     *
     * @return 通用缓存配置
     */
    public RedisCacheConfiguration entityCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(24))
                .disableCachingNullValues();
    }
}
