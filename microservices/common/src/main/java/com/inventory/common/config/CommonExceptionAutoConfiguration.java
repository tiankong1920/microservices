package com.inventory.common.config;

import com.inventory.common.core.UnifiedGlobalExceptionHandler;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 通用异常处理自动配置，确保 UnifiedGlobalExceptionHandler 被所有服务自动发现.
 *
 * <p>由于各微服务的 @SpringBootApplication 默认只扫描自身包路径，
 * 通过 Spring Boot 3.x 的 AutoConfiguration.imports 机制注册此配置，
 * 使 common 模块中的统一异常处理器对所有依赖服务生效。</p>
 */
@AutoConfiguration
@Import(UnifiedGlobalExceptionHandler.class)
public class CommonExceptionAutoConfiguration {
}
