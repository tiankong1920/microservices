package com.inventory.registryservice.dto;

import java.util.List;

/**
 * Service info DTO with instance list.
 */
public record ServiceInfoDTO(
    String serviceName,
    String groupName,
    String namespaceId,
    int instanceCount,
    int healthyInstanceCount,
    boolean healthy,
    List<ServiceInstanceDTO> instances
) {
}
