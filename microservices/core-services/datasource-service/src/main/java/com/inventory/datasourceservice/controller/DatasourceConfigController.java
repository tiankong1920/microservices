package com.inventory.datasourceservice.controller;

import com.inventory.datasourceservice.dto.ApiResponse;
import com.inventory.datasourceservice.dto.DatasourceConfigDTO;
import com.inventory.datasourceservice.dto.PageResponse;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.service.DatasourceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据源配置控制器 - 管理数据源配置的增删改查
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/datasources")
@RequiredArgsConstructor
@Tag(name = "数据源配置管理", description = "数据源配置的增删改查接口")
@Validated
public class DatasourceConfigController {

    private final DatasourceConfigService datasourceConfigService;

    /**
     * 创建数据源
     *
     * @param dto 数据源配置数据
     * @return 创建的数据源信息
     */
    @PostMapping
    @Operation(summary = "创建数据源", description = "创建新的数据源配置")
    public ResponseEntity<ApiResponse<DatasourceConfigDTO>> createDatasource(
            @Valid @RequestBody DatasourceConfigDTO dto) {
        DatasourceConfigDTO created = datasourceConfigService.createDatasource(dto);
        return ResponseEntity.ok(ApiResponse.success("数据源创建成功", created));
    }

    /**
     * 更新数据源
     *
     * @param id 数据源ID
     * @param dto 更新后的数据源配置数据
     * @return 更新后的数据源信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新数据源", description = "更新指定数据源的配置")
    public ResponseEntity<ApiResponse<DatasourceConfigDTO>> updateDatasource(
            @Parameter(description = "数据源ID") @PathVariable Long id,
            @Valid @RequestBody DatasourceConfigDTO dto) {
        DatasourceConfigDTO updated = datasourceConfigService.updateDatasource(id, dto);
        return ResponseEntity.ok(ApiResponse.success("数据源更新成功", updated));
    }

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据源", description = "删除指定的数据源配置")
    public ResponseEntity<ApiResponse<Void>> deleteDatasource(
            @Parameter(description = "数据源ID") @PathVariable Long id) {
        datasourceConfigService.deleteDatasource(id);
        return ResponseEntity.ok(ApiResponse.success("数据源删除成功", null));
    }

    /**
     * 获取数据源详情
     *
     * @param id 数据源ID
     * @return 数据源详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取数据源详情", description = "根据ID获取数据源详细信息")
    public ResponseEntity<ApiResponse<DatasourceConfigDTO>> getDatasource(
            @Parameter(description = "数据源ID") @PathVariable Long id) {
        DatasourceConfigDTO dto = datasourceConfigService.getDatasource(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * 获取数据源列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 分页数据源列表
     */
    @GetMapping
    @Operation(summary = "获取数据源列表", description = "分页获取数据源列表")
    public ResponseEntity<ApiResponse<PageResponse<DatasourceConfigDTO>>> listDatasources(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<DatasourceConfigDTO> result = datasourceConfigService.listDatasources(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 搜索数据源
     *
     * @param name 数据源名称
     * @param type 数据源类型
     * @param status 数据源状态
     * @param page 页码
     * @param size 每页大小
     * @return 符合条件的数据源列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索数据源", description = "根据条件搜索数据源")
    public ResponseEntity<ApiResponse<PageResponse<DatasourceConfigDTO>>> searchDatasources(
            @Parameter(description = "名称") @RequestParam(required = false) String name,
            @Parameter(description = "类型") @RequestParam(required = false) DatasourceConfig.DatasourceType type,
            @Parameter(description = "状态") @RequestParam(required = false) DatasourceConfig.DatasourceStatus status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        PageResponse<DatasourceConfigDTO> result = datasourceConfigService.searchDatasources(name, type, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 按类型获取数据源
     *
     * @param type 数据源类型
     * @return 指定类型的数据源列表
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "按类型获取数据源", description = "获取指定类型的所有数据源")
    public ResponseEntity<ApiResponse<java.util.List<DatasourceConfigDTO>>> listByType(
            @Parameter(description = "数据源类型") @PathVariable DatasourceConfig.DatasourceType type) {
        java.util.List<DatasourceConfigDTO> result = datasourceConfigService.listByType(type);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取配置模板
     *
     * @param type 数据源类型
     * @return 指定类型的配置字段定义
     */
    @GetMapping("/config-schema/{type}")
    @Operation(summary = "获取配置模板", description = "获取指定类型数据源的配置字段定义")
    public ResponseEntity<ApiResponse<Object>> getConfigSchema(
            @Parameter(description = "数据源类型") @PathVariable DatasourceConfig.DatasourceType type) {
        Object schema = datasourceConfigService.getConfigSchema(type);
        return ResponseEntity.ok(ApiResponse.success(schema));
    }
}
