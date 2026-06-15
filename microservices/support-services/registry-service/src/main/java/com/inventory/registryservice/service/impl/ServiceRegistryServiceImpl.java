package com.inventory.registryservice.service.impl;

import com.inventory.registryservice.dto.ServiceInfoDTO;
import com.inventory.registryservice.dto.ServiceInstanceDTO;
import com.inventory.registryservice.service.ServiceRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service registry service implementation using Spring Cloud DiscoveryClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ServiceRegistryServiceImpl implements ServiceRegistryService {

    private final DiscoveryClient discoveryClient;

    @Override
    public List<String> getAllServiceNames() {
        log.info("Fetching all registered service names");
        final List<String> services = discoveryClient.getServices();
        log.info("Found {} registered services", services.size());
        return services;
    }

    @Override
    public ServiceInfoDTO getServiceInfo(final String serviceName) {
        log.info("Fetching service info for: {}", serviceName);
        final List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        final List<ServiceInstanceDTO> instanceDTOs = instances.stream()
                .map(this::toServiceInstanceDTO)
                .collect(Collectors.toList());

        final long healthyCount = instanceDTOs.stream()
                .filter(ServiceInstanceDTO::healthy)
                .count();

        return new ServiceInfoDTO(
                serviceName,
                "DEFAULT_GROUP",
                "public",
                instances.size(),
                (int) healthyCount,
                healthyCount > 0,
                instanceDTOs
        );
    }

    @Override
    public List<ServiceInstanceDTO> getServiceInstances(final String serviceName) {
        log.info("Fetching instances for service: {}", serviceName);
        final List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        return instances.stream()
                .map(this::toServiceInstanceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceInfoDTO> getAllServicesHealth() {
        log.info("Fetching health status for all services");
        final List<String> serviceNames = discoveryClient.getServices();
        return serviceNames.stream()
                .map(this::getServiceInfo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isServiceHealthy(final String serviceName) {
        final List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            log.warn("Service not found: {}", serviceName);
            return false;
        }
        final boolean healthy = instances.stream().anyMatch(instance -> {
            String up = instance.getMetadata().getOrDefault("up", "true");
            return Boolean.parseBoolean(up);
        });
        log.info("Service {} health status: {}", serviceName, healthy);
        return healthy;
    }

    private ServiceInstanceDTO toServiceInstanceDTO(final ServiceInstance instance) {
        final Map<String, String> metadata = instance.getMetadata();
        final boolean isUp = Boolean.parseBoolean(metadata.getOrDefault("up", "true"));
        return new ServiceInstanceDTO(
                instance.getServiceId(),
                instance.getInstanceId(),
                instance.getHost(),
                instance.getPort(),
                instance.getHost(),
                isUp,
                metadata.getOrDefault("cluster", "DEFAULT"),
                metadata.getOrDefault("namespace", "public"),
                metadata.getOrDefault("group", "DEFAULT_GROUP"),
                Double.parseDouble(metadata.getOrDefault("weight", "1.0")),
                Boolean.parseBoolean(metadata.getOrDefault("enabled", "true")),
                Boolean.parseBoolean(metadata.getOrDefault("ephemeral", "true"))
        );
    }
}
