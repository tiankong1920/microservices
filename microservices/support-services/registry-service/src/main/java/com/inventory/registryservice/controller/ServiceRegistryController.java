package com.inventory.registryservice.controller;

import com.inventory.common.core.ApiResponse;
import com.inventory.registryservice.dto.ServiceInfoDTO;
import com.inventory.registryservice.dto.ServiceInstanceDTO;
import com.inventory.registryservice.service.ServiceRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Service registry REST controller.
 */
@RestController
@RequestMapping("/api/v1/registry")
@RequiredArgsConstructor
@Tag(name = "Service Registry", description = "Service discovery and health check APIs")
@SuppressWarnings("null")
public class ServiceRegistryController {

    private final ServiceRegistryService registryService;

    @GetMapping("/services")
    @Operation(summary = "获取所有已注册服务", description = "返回Nacos中注册的所有服务名称列表")
    public ResponseEntity<ApiResponse<List<String>>> getAllServices() {
        return ResponseEntity.ok(ApiResponse.success(registryService.getAllServiceNames()));
    }

    @GetMapping("/services/{serviceName}")
    @Operation(summary = "获取服务信息", description = "返回服务详细信息，包括所有实例")
    public ResponseEntity<ApiResponse<ServiceInfoDTO>> getServiceInfo(
            @PathVariable String serviceName) {
        return ResponseEntity.ok(ApiResponse.success(registryService.getServiceInfo(serviceName)));
    }

    @GetMapping("/services/{serviceName}/instances")
    @Operation(summary = "获取服务实例列表", description = "返回特定服务的所有实例")
    public ResponseEntity<ApiResponse<List<ServiceInstanceDTO>>> getServiceInstances(
            @PathVariable String serviceName) {
        return ResponseEntity.ok(ApiResponse.success(registryService.getServiceInstances(serviceName)));
    }

    @GetMapping("/health/all")
    @Operation(summary = "获取所有服务健康状态", description = "返回所有已注册服务的健康状态")
    public ResponseEntity<ApiResponse<List<ServiceInfoDTO>>> getAllServicesHealth() {
        return ResponseEntity.ok(ApiResponse.success(registryService.getAllServicesHealth()));
    }

    @GetMapping("/health/{serviceName}")
    @Operation(summary = "检查服务健康状态", description = "返回特定服务是否健康")
    public ResponseEntity<ApiResponse<Boolean>> isServiceHealthy(
            @PathVariable String serviceName) {
        return ResponseEntity.ok(ApiResponse.success(registryService.isServiceHealthy(serviceName)));
    }
}
