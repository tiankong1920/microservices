package com.inventory.common.config;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Custom cache key generator that generates unique keys for cache entries.
 */
@Component("customCacheKeyGenerator")
public class CustomCacheKeyGenerator implements KeyGenerator {

    @Override
    @NonNull
    public Object generate(@NonNull final Object target, @NonNull final Method method,
                          @NonNull final Object... params) {
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(target.getClass().getSimpleName())
                 .append('.')
                 .append(method.getName());

        for (final Object param : params) {
            if (param != null) {
                keyBuilder.append(':')
                         .append(param.toString());
            }
        }

        return keyBuilder;
    }
}
