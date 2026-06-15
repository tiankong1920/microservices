package com.inventory.configservice.controller;

import com.inventory.common.core.ApiResponse;
import com.inventory.configservice.dto.ConfigDTO;
import com.inventory.configservice.dto.ConfigHistoryDTO;
import com.inventory.configservice.service.ConfigManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration management REST controller.
 */
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Tag(name = "Configuration Management", description = "Nacos configuration management APIs")
@SuppressWarnings("null")
public class ConfigManagementController {

    private final ConfigManagementService configService;

    @Data
    public static class PublishConfigRequest {
        @NotBlank(message = "dataId is required")
        private String dataId;

        @NotBlank(message = "group is required")
        private String group;

        private String content;

        private String type = "yaml";

        private String description;
    }

    @GetMapping("/{dataId}")
    @Operation(summary = "获取配置", description = "根据dataId获取配置内容")
    public ResponseEntity<ApiResponse<String>> getConfig(
            @PathVariable String dataId,
            @RequestParam(defaultValue = "DEFAULT_GROUP") String group) {
        final String content = configService.getConfig(dataId, group);
        if (content == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("CONFIG_NOT_FOUND", "Configuration not found: " + dataId));
        }
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/{dataId}/detail")
    @Operation(summary = "获取配置详情", description = "获取配置及元数据")
    public ResponseEntity<ApiResponse<ConfigDTO>> getConfigDetail(
            @PathVariable String dataId,
            @RequestParam(defaultValue = "DEFAULT_GROUP") String group,
            @RequestParam(required = false) String namespace) {
        return ResponseEntity.ok(ApiResponse.success(configService.getConfigDetail(dataId, group, namespace)));
    }

    @PostMapping
    @Operation(summary = "发布配置", description = "创建或更新配置")
    public ResponseEntity<ApiResponse<Map<String, Object>>> publishConfig(
            @Valid @RequestBody PublishConfigRequest request) {
        final boolean success = configService.publishConfig(
                request.getDataId(), request.getGroup(), request.getContent(), request.getType());

        final Map<String, Object> result = new HashMap<>();
        result.put("success", success);

        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Configuration published successfully", result));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("PUBLISH_FAILED", "Failed to publish configuration", result));
    }

    @DeleteMapping("/{dataId}")
    @Operation(summary = "删除配置", description = "删除配置")
    public ResponseEntity<ApiResponse<Boolean>> removeConfig(
            @PathVariable String dataId,
            @RequestParam(defaultValue = "DEFAULT_GROUP") String group) {
        final boolean success = configService.removeConfig(dataId, group);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(success));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("DELETE_FAILED", "Failed to delete configuration", false));
    }

    @GetMapping("/history")
    @Operation(summary = "获取配置历史", description = "获取配置的变更历史")
    public ResponseEntity<ApiResponse<List<ConfigHistoryDTO>>> getConfigHistory(
            @RequestParam String dataId,
            @RequestParam(defaultValue = "DEFAULT_GROUP") String group,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(configService.getConfigHistory(dataId, group, page, pageSize)));
    }

    @GetMapping("/list")
    @Operation(summary = "获取配置列表", description = "获取分组下的所有配置")
    public ResponseEntity<ApiResponse<List<ConfigDTO>>> listConfigs(
            @RequestParam(defaultValue = "DEFAULT_GROUP") String group,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(configService.listConfigs(group, page, pageSize)));
    }
}
