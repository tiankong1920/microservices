package com.inventory.registryservice.controller;

import com.inventory.registryservice.dto.ServiceInfoDTO;
import com.inventory.registryservice.dto.ServiceInstanceDTO;
import com.inventory.registryservice.service.ServiceRegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ServiceRegistryControllerTest {

    @Mock
    private ServiceRegistryService mockRegistryService;

    private ServiceRegistryController controller;

    @BeforeEach
    void setUp() {
        controller = new ServiceRegistryController(mockRegistryService);
    }

    @Test
    void testGetAllServicesReturnsSuccess() {
        List<String> services = Arrays.asList("product-service", "order-service");
        when(mockRegistryService.getAllServiceNames()).thenReturn(services);

        ResponseEntity<?> response = controller.getAllServices();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(mockRegistryService).getAllServiceNames();
    }

    @Test
    void testGetAllServicesReturnsEmptyList() {
        when(mockRegistryService.getAllServiceNames()).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.getAllServices();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testGetServiceInfoReturnsSuccess() {
        ServiceInfoDTO serviceInfo = new ServiceInfoDTO(
                "product-service",
                "DEFAULT_GROUP",
                "public",
                2,
                2,
                true,
                Collections.emptyList()
        );
        when(mockRegistryService.getServiceInfo("product-service")).thenReturn(serviceInfo);

        ResponseEntity<?> response = controller.getServiceInfo("product-service");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(mockRegistryService).getServiceInfo("product-service");
    }

    @Test
    void testGetServiceInstancesReturnsSuccess() {
        List<ServiceInstanceDTO> instances = Arrays.asList(
                new ServiceInstanceDTO("product-service", "instance-1", "192.168.1.10", 8081,
                        "192.168.1.10", true, "DEFAULT", "public", "DEFAULT_GROUP", 1.0, true, true),
                new ServiceInstanceDTO("product-service", "instance-2", "192.168.1.11", 8081,
                        "192.168.1.11", true, "DEFAULT", "public", "DEFAULT_GROUP", 1.0, true, true)
        );
        when(mockRegistryService.getServiceInstances("product-service")).thenReturn(instances);

        ResponseEntity<?> response = controller.getServiceInstances("product-service");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockRegistryService).getServiceInstances("product-service");
    }

    @Test
    void testGetAllServicesHealthReturnsSuccess() {
        List<ServiceInfoDTO> healthList = Collections.singletonList(
                new ServiceInfoDTO("product-service", "DEFAULT_GROUP", "public", 1, 1, true, Collections.emptyList())
        );
        when(mockRegistryService.getAllServicesHealth()).thenReturn(healthList);

        ResponseEntity<?> response = controller.getAllServicesHealth();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockRegistryService).getAllServicesHealth();
    }

    @Test
    void testIsServiceHealthyReturnsTrue() {
        when(mockRegistryService.isServiceHealthy("product-service")).thenReturn(true);

        ResponseEntity<?> response = controller.isServiceHealthy("product-service");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockRegistryService).isServiceHealthy("product-service");
    }

    @Test
    void testIsServiceHealthyReturnsFalse() {
        when(mockRegistryService.isServiceHealthy("nonexistent-service")).thenReturn(false);

        ResponseEntity<?> response = controller.isServiceHealthy("nonexistent-service");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
