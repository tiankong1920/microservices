package com.inventory.gatewayservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * IP过滤器配置类.
 */
@Component
@ConfigurationProperties(prefix = "gateway.ip-filter")
@Getter
@Setter
public class IpFilterConfig {
    /**
 * 是否启用IP过滤.
     */
    private boolean enabled = false;

    /**
 * IP过滤策略：.
 * - whitelist: 白名单模式，只允许白名单中的IP访问
 * - blacklist: 黑名单模式，拒绝黑名单中的IP访问
     */
    private String strategy = "whitelist";

    /**
 * IP白名单列表.
     */
    private List<String> whitelist;

    /**
 * IP黑名单列表.
     */
    private List<String> blacklist;

    /**
 * 是否允许本地IP访问.
     */
    private boolean allowLocalhost = true;

    /**
 * 是否记录IP过滤日志.
     */
    private boolean logEnabled = true;
}
