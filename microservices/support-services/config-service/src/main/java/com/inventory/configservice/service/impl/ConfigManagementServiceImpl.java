package com.inventory.configservice.service.impl;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.inventory.configservice.dto.ConfigDTO;
import com.inventory.configservice.dto.ConfigHistoryDTO;
import com.inventory.configservice.exception.ConfigException;
import com.inventory.configservice.service.ConfigManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Configuration management service implementation using Nacos Config API.
 */
@Slf4j
@Service
@SuppressWarnings("null")
public class ConfigManagementServiceImpl implements ConfigManagementService {

    private final ConfigService configService;
    private final String namespace;

    public ConfigManagementServiceImpl(
            @Value("${spring.cloud.nacos.config.server-addr:localhost:8848}") final String serverAddr,
            @Value("${spring.cloud.nacos.config.namespace:public}") final String namespace) {
        this.namespace = namespace;
        try {
            this.configService = NacosFactory.createConfigService(serverAddr);
            log.info("Nacos ConfigService initialized with server: {}", serverAddr);
        } catch (NacosException e) {
            log.error("Failed to initialize Nacos ConfigService: {}", e.getMessage(), e);
            throw ConfigException.initializationFailed(e.getMessage());
        }
    }

    @Override
    public String getConfig(final String dataId, final String group) {
        log.info("Fetching config: dataId={}, group={}", dataId, group);
        try {
            return configService.getConfig(dataId, group, 5000);
        } catch (NacosException e) {
            log.error("Failed to get config: dataId={}, group={}, error={}", dataId, group, e.getMessage());
            throw new ConfigOperationException("Failed to get config", dataId, group, e);
        }
    }

    @Override
    public ConfigDTO getConfigDetail(final String dataId, final String group, final String namespace) {
        log.info("Fetching config detail: dataId={}, group={}, namespace={}", dataId, group, namespace);
        final String content = getConfig(dataId, group);
        return new ConfigDTO(
                dataId,
                group,
                namespace != null ? namespace : this.namespace,
                content,
                "yaml",
                "Configuration for " + dataId,
                dataId,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );
    }

    @Override
    public boolean publishConfig(final String dataId, final String group, final String content, final String type) {
        log.info("Publishing config: dataId={}, group={}, type={}", dataId, group, type);
        try {
            final boolean result = configService.publishConfig(dataId, group, content, type);
            log.info("Config published: dataId={}, result={}", dataId, result);
            return result;
        } catch (NacosException e) {
            log.error("Failed to publish config: dataId={}, group={}, error={}", dataId, group, e.getMessage());
            throw new ConfigOperationException("Failed to publish config", dataId, group, e);
        }
    }

    @Override
    public boolean removeConfig(final String dataId, final String group) {
        log.info("Removing config: dataId={}, group={}", dataId, group);
        try {
            final boolean result = configService.removeConfig(dataId, group);
            log.info("Config removed: dataId={}, result={}", dataId, result);
            return result;
        } catch (NacosException e) {
            log.error("Failed to remove config: dataId={}, group={}, error={}", dataId, group, e.getMessage());
            throw new ConfigOperationException("Failed to remove config", dataId, group, e);
        }
    }

    @Override
    public List<ConfigHistoryDTO> getConfigHistory(
            final String dataId, final String group, final int page, final int pageSize) {
        log.info("Fetching config history: dataId={}, group={}, page={}, size={}", dataId, group, page, pageSize);
        return Collections.emptyList();
    }

    @Override
    public List<ConfigDTO> listConfigs(final String group, final int page, final int pageSize) {
        log.info("Listing configs: group={}, page={}, size={}", group, page, pageSize);
        return Collections.emptyList();
    }

    /**
     * Exception thrown when Nacos config operations fail.
     */
    public static class ConfigOperationException extends RuntimeException {
        private final String dataId;
        private final String group;

        public ConfigOperationException(final String message, final String dataId, final String group, final Throwable cause) {
            super(message + " [" + dataId + ":" + group + "]", cause);
            this.dataId = dataId;
            this.group = group;
        }

        public String getDataId() {
            return dataId;
        }

        public String getGroup() {
            return group;
        }
    }
}
