package com.invoice.invoiceservice.controller;

import java.util.List;

import com.invoice.invoiceservice.dto.OperationLogDTO;
import com.invoice.invoiceservice.entity.OperationLog;
import com.invoice.invoiceservice.repository.IOperationLogRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoice/operation-logs")
@RequiredArgsConstructor
@Tag(name = "Operation Log", description = "操作日志API")
@Validated
public class OperationLogController {

    private final IOperationLogRepository operationLogRepository;

    /**
     * 查询实体操作日志
     *
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 操作日志列表
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "查询实体操作日志")
    public ResponseEntity<List<OperationLogDTO>> getEntityLogs(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<OperationLogDTO> logs = operationLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        OperationLog.EntityType.valueOf(entityType), entityId)
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(logs);
    }

    /**
     * 查询操作人日志
     *
     * @param operator 操作人
     * @return 操作日志列表
     */
    @GetMapping("/operator/{operator}")
    @Operation(summary = "查询操作人日志")
    public ResponseEntity<List<OperationLogDTO>> getOperatorLogs(@PathVariable String operator) {
        List<OperationLogDTO> logs = operationLogRepository
                .findByOperatorOrderByCreatedAtDesc(operator)
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(logs);
    }

    /**
     * 按时间范围查询日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作日志列表
     */
    @GetMapping("/time-range")
    @Operation(summary = "按时间范围查询日志")
    public ResponseEntity<List<OperationLogDTO>> getLogsByTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        List<OperationLogDTO> logs = operationLogRepository
                .findByTimeRange(
                        java.time.LocalDateTime.parse(startTime),
                        java.time.LocalDateTime.parse(endTime))
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(logs);
    }

    private OperationLogDTO mapToDTO(OperationLog entity) {
        return OperationLogDTO.builder()
                .id(entity.getId())
                .entityType(entity.getEntityType().name())
                .entityId(entity.getEntityId())
                .operationType(entity.getOperationType().name())
                .operator(entity.getOperator())
                .ipAddress(entity.getIpAddress())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .description(entity.getDescription())
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
