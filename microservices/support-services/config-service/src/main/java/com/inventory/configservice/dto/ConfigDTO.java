package com.inventory.configservice.dto;

/**
 * Configuration item DTO.
 */
public record ConfigDTO(
    String dataId,
    String group,
    String namespace,
    String content,
    String type,
    String description,
    String appName,
    long createTime,
    long updateTime
) {
}
