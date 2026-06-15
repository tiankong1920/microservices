package com.inventory.datasourceservice.controller;

import com.inventory.datasourceservice.dto.ApiResponse;
import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.dto.PageResponse;
import com.inventory.datasourceservice.entity.ConnectionTestLog;
import com.inventory.datasourceservice.service.ConnectionTestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 连接测试控制器 - 管理数据源连接测试
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/connection-test")
@RequiredArgsConstructor
@Tag(name = "连接测试", description = "数据源连接测试接口")
@Validated
public class ConnectionTestController {

    private final ConnectionTestService connectionTestService;

    /**
     * 测试单个连接
     *
     * @param datasourceId 数据源ID
     * @return 连接测试结果
     */
    @PostMapping("/{datasourceId}")
    @Operation(summary = "测试单个连接", description = "测试指定数据源的连接状态")
    public ResponseEntity<ApiResponse<ConnectionTestResultDTO>> testConnection(
            @Parameter(description = "数据源ID") @PathVariable Long datasourceId) {
        ConnectionTestResultDTO result = connectionTestService.testConnection(datasourceId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 批量测试连接
     *
     * @param datasourceIds 数据源ID列表
     * @return 批量连接测试结果
     */
    @PostMapping("/batch")
    @Operation(summary = "批量测试连接", description = "批量测试多个数据源的连接状态")
    public ResponseEntity<ApiResponse<List<ConnectionTestResultDTO>>> batchTestConnections(
            @RequestBody List<Long> datasourceIds) {
        List<ConnectionTestResultDTO> results = connectionTestService.batchTestConnections(datasourceIds);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * 获取测试历史
     *
     * @param datasourceId 数据源ID
     * @param page 页码
     * @param size 每页大小
     * @param result 测试结果
     * @param testType 测试类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 连接测试历史记录
     */
    @GetMapping("/{datasourceId}/history")
    @Operation(summary = "获取测试历史", description = "获取指定数据源的连接测试历史记录")
    public ResponseEntity<ApiResponse<PageResponse<ConnectionTestLog>>> getTestHistory(
            @Parameter(description = "数据源ID") @PathVariable Long datasourceId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "测试结果") @RequestParam(required = false) ConnectionTestLog.TestResult result,
            @Parameter(description = "测试类型") @RequestParam(required = false) ConnectionTestLog.TestType testType,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        PageResponse<ConnectionTestLog> history = connectionTestService.getTestHistory(
                datasourceId, page, size, result, testType, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * 获取测试统计
     *
     * @param datasourceId 数据源ID
     * @param since 统计起始时间
     * @return 连接测试统计数据
     */
    @GetMapping("/{datasourceId}/statistics")
    @Operation(summary = "获取测试统计", description = "获取指定数据源的连接测试统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTestStatistics(
            @Parameter(description = "数据源ID") @PathVariable Long datasourceId,
            @Parameter(description = "统计起始时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        if (since == null) {
            since = LocalDateTime.now().minusDays(7);
        }
        Map<String, Object> stats = connectionTestService.getTestStatistics(datasourceId, since);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
