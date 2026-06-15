package com.inventory.registryservice.service.impl;

import com.inventory.registryservice.dto.ServiceInfoDTO;
import com.inventory.registryservice.dto.ServiceInstanceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ServiceRegistryServiceImplTest {

    @Mock
    private DiscoveryClient mockDiscoveryClient;

    private ServiceRegistryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ServiceRegistryServiceImpl(mockDiscoveryClient);
    }

    @Test
    void testGetAllServiceNamesReturnsEmptyList() {
        when(mockDiscoveryClient.getServices()).thenReturn(Collections.emptyList());

        List<String> result = service.getAllServiceNames();

        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllServiceNamesReturnsMultipleServices() {
        List<String> services = Arrays.asList("product-service", "order-service", "inventory-service");
        when(mockDiscoveryClient.getServices()).thenReturn(services);

        List<String> result = service.getAllServiceNames();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("product-service", "order-service", "inventory-service");
    }

    @Test
    void testGetServiceInfoWithNoInstances() {
        when(mockDiscoveryClient.getInstances("unknown-service")).thenReturn(Collections.emptyList());

        ServiceInfoDTO result = service.getServiceInfo("unknown-service");

        assertThat(result).isNotNull();
        assertThat(result.serviceName()).isEqualTo("unknown-service");
        assertThat(result.instanceCount()).isZero();
        assertThat(result.healthyInstanceCount()).isZero();
        assertThat(result.healthy()).isFalse();
        assertThat(result.instances()).isEmpty();
    }

    @Test
    void testGetServiceInfoWithHealthyInstance() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("cluster", "DEFAULT");
        metadata.put("namespace", "public");
        metadata.put("group", "DEFAULT_GROUP");
        metadata.put("weight", "1.0");
        metadata.put("enabled", "true");
        metadata.put("ephemeral", "true");

        ServiceInstance instance = new DefaultServiceInstance(
                "instance-1",
                "product-service",
                "192.168.1.10",
                8081,
                false,
                metadata
        );

        when(mockDiscoveryClient.getInstances("product-service")).thenReturn(Collections.singletonList(instance));

        ServiceInfoDTO result = service.getServiceInfo("product-service");

        assertThat(result).isNotNull();
        assertThat(result.serviceName()).isEqualTo("product-service");
        assertThat(result.instanceCount()).isEqualTo(1);
        assertThat(result.instances()).hasSize(1);

        ServiceInstanceDTO instanceDTO = result.instances().get(0);
        assertThat(instanceDTO.serviceId()).isEqualTo("product-service");
        assertThat(instanceDTO.host()).isEqualTo("192.168.1.10");
        assertThat(instanceDTO.port()).isEqualTo(8081);
        assertThat(instanceDTO.healthy()).isTrue();
    }

    @Test
    void testGetServiceInfoWithMixedHealthInstances() {
        Map<String, String> healthyMetadata = new HashMap<>();
        healthyMetadata.put("cluster", "DEFAULT");
        healthyMetadata.put("namespace", "public");
        healthyMetadata.put("group", "DEFAULT_GROUP");
        healthyMetadata.put("weight", "1.0");
        healthyMetadata.put("enabled", "true");
        healthyMetadata.put("ephemeral", "true");
        healthyMetadata.put("up", "true");

        Map<String, String> unhealthyMetadata = new HashMap<>();
        unhealthyMetadata.put("cluster", "DEFAULT");
        unhealthyMetadata.put("namespace", "public");
        unhealthyMetadata.put("group", "DEFAULT_GROUP");
        unhealthyMetadata.put("up", "false");

        ServiceInstance healthyInstance = new DefaultServiceInstance(
                "instance-1",
                "order-service",
                "192.168.1.20",
                8082,
                false,
                healthyMetadata
        );

        ServiceInstance unhealthyInstance = new DefaultServiceInstance(
                "instance-2",
                "order-service",
                "192.168.1.21",
                8082,
                true,
                unhealthyMetadata
        );

        when(mockDiscoveryClient.getInstances("order-service"))
                .thenReturn(Arrays.asList(healthyInstance, unhealthyInstance));

        ServiceInfoDTO result = service.getServiceInfo("order-service");

        assertThat(result).isNotNull();
        assertThat(result.instanceCount()).isEqualTo(2);
        assertThat(result.healthyInstanceCount()).isEqualTo(1);
        assertThat(result.healthy()).isTrue();
    }

    @Test
    void testGetServiceInstancesReturnsAllInstances() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("cluster", "DEFAULT");
        metadata.put("namespace", "public");
        metadata.put("group", "DEFAULT_GROUP");
        metadata.put("weight", "1.0");
        metadata.put("enabled", "true");
        metadata.put("ephemeral", "true");

        ServiceInstance instance1 = new DefaultServiceInstance(
                "instance-1",
                "product-service",
                "192.168.1.10",
                8081,
                false,
                metadata
        );
        ServiceInstance instance2 = new DefaultServiceInstance(
                "instance-2",
                "product-service",
                "192.168.1.11",
                8081,
                false,
                metadata
        );

        when(mockDiscoveryClient.getInstances("product-service"))
                .thenReturn(Arrays.asList(instance1, instance2));

        List<ServiceInstanceDTO> result = service.getServiceInstances("product-service");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).host()).isEqualTo("192.168.1.10");
        assertThat(result.get(1).host()).isEqualTo("192.168.1.11");
    }

    @Test
    void testGetServiceInstancesReturnsEmptyList() {
        when(mockDiscoveryClient.getInstances("nonexistent-service")).thenReturn(Collections.emptyList());

        List<ServiceInstanceDTO> result = service.getServiceInstances("nonexistent-service");

        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllServicesHealthReturnsEmptyList() {
        when(mockDiscoveryClient.getServices()).thenReturn(Collections.emptyList());

        List<ServiceInfoDTO> result = service.getAllServicesHealth();

        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllServicesHealthReturnsMultipleServices() {
        when(mockDiscoveryClient.getServices()).thenReturn(Arrays.asList("product-service", "order-service"));
        when(mockDiscoveryClient.getInstances("product-service")).thenReturn(Collections.emptyList());
        when(mockDiscoveryClient.getInstances("order-service")).thenReturn(Collections.emptyList());

        List<ServiceInfoDTO> result = service.getAllServicesHealth();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).serviceName()).isEqualTo("product-service");
        assertThat(result.get(1).serviceName()).isEqualTo("order-service");
    }

    @Test
    void testIsServiceHealthyReturnsTrueForHealthyService() {
        ServiceInstance instance = new DefaultServiceInstance(
                "instance-1",
                "product-service",
                "192.168.1.10",
                8081,
                false,
                new HashMap<>()
        );

        when(mockDiscoveryClient.getInstances("product-service")).thenReturn(Collections.singletonList(instance));

        boolean result = service.isServiceHealthy("product-service");

        assertThat(result).isTrue();
    }

    @Test
    void testIsServiceHealthyReturnsFalseForUnhealthyService() {
        Map<String, String> unhealthyMetadata = new HashMap<>();
        unhealthyMetadata.put("up", "false");
        ServiceInstance instance = new DefaultServiceInstance(
                "instance-1",
                "product-service",
                "192.168.1.10",
                8081,
                false,
                unhealthyMetadata
        );

        when(mockDiscoveryClient.getInstances("product-service")).thenReturn(Collections.singletonList(instance));

        boolean result = service.isServiceHealthy("product-service");

        assertThat(result).isFalse();
    }

    @Test
    void testIsServiceHealthyReturnsFalseForNonexistentService() {
        when(mockDiscoveryClient.getInstances("nonexistent-service")).thenReturn(Collections.emptyList());

        boolean result = service.isServiceHealthy("nonexistent-service");

        assertThat(result).isFalse();
    }

    @Test
    void testGetServiceInstanceDTOWithDefaultMetadata() {
        Map<String, String> emptyMetadata = new HashMap<>();
        ServiceInstance instance = new DefaultServiceInstance(
                "instance-1",
                "test-service",
                "10.0.0.1",
                9090,
                false,
                emptyMetadata
        );

        when(mockDiscoveryClient.getInstances("test-service")).thenReturn(Collections.singletonList(instance));

        List<ServiceInstanceDTO> result = service.getServiceInstances("test-service");

        assertThat(result).hasSize(1);
        ServiceInstanceDTO dto = result.get(0);
        assertThat(dto.serviceId()).isEqualTo("test-service");
        assertThat(dto.host()).isEqualTo("10.0.0.1");
        assertThat(dto.port()).isEqualTo(9090);
        assertThat(dto.healthy()).isTrue();
        assertThat(dto.clusterName()).isEqualTo("DEFAULT");
        assertThat(dto.namespaceId()).isEqualTo("public");
        assertThat(dto.groupName()).isEqualTo("DEFAULT_GROUP");
        assertThat(dto.weight()).isEqualTo(1.0);
        assertThat(dto.enabled()).isTrue();
        assertThat(dto.ephemeral()).isTrue();
    }
}
