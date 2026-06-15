package com.inventory.configservice.service;

import com.inventory.configservice.dto.ConfigDTO;
import com.inventory.configservice.dto.ConfigHistoryDTO;

import java.util.List;

/**
 * Configuration management service interface.
 */
public interface ConfigManagementService {

    /**
     * Get configuration content by dataId and group.
     */
    String getConfig(String dataId, String group);

    /**
     * Get configuration with metadata.
     */
    ConfigDTO getConfigDetail(String dataId, String group, String namespace);

    /**
     * Publish or update configuration.
     */
    boolean publishConfig(String dataId, String group, String content, String type);

    /**
     * Delete configuration.
     */
    boolean removeConfig(String dataId, String group);

    /**
     * Get configuration history.
     */
    List<ConfigHistoryDTO> getConfigHistory(String dataId, String group, int page, int pageSize);

    /**
     * List all configurations in a group.
     */
    List<ConfigDTO> listConfigs(String group, int page, int pageSize);
}
