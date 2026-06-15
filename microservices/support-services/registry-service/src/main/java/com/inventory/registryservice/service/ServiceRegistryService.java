package com.inventory.registryservice.service;

import com.inventory.registryservice.dto.ServiceInfoDTO;
import com.inventory.registryservice.dto.ServiceInstanceDTO;

import java.util.List;

/**
 * Service registry service interface.
 */
public interface ServiceRegistryService {

    /**
     * Get all registered service names.
     */
    List<String> getAllServiceNames();

    /**
     * Get service info with instances.
     */
    ServiceInfoDTO getServiceInfo(String serviceName);

    /**
     * Get all instances of a service.
     */
    List<ServiceInstanceDTO> getServiceInstances(String serviceName);

    /**
     * Get health status of all registered services.
     */
    List<ServiceInfoDTO> getAllServicesHealth();

    /**
     * Check if a specific service is healthy.
     */
    boolean isServiceHealthy(String serviceName);
}
