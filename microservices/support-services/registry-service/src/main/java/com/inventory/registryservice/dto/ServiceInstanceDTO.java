package com.inventory.registryservice.dto;

/**
 * Service instance DTO.
 */
public record ServiceInstanceDTO(
    String serviceId,
    String instanceId,
    String host,
    int port,
    String ip,
    boolean healthy,
    String clusterName,
    String namespaceId,
    String groupName,
    double weight,
    boolean enabled,
    boolean ephemeral
) {
}
