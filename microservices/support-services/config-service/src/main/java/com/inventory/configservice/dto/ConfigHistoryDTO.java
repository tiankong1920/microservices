package com.inventory.configservice.dto;

/**
 * Configuration history entry DTO.
 */
public record ConfigHistoryDTO(
    long nid,
    String dataId,
    String group,
    String namespace,
    String content,
    String opType,
    String srcIp,
    String srcUser,
    long gmtCreated,
    long gmtModified
) {
}
