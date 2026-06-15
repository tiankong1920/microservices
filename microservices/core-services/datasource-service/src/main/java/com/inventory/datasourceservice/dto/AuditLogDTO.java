package com.inventory.datasourceservice.dto;

import com.inventory.datasourceservice.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;

    private String userId;

    private String username;

    private AuditLog.Operation operation;

    private String resourceType;

    private String resourceId;

    private String oldValue;

    private String newValue;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime createdAt;

    public static AuditLogDTO fromEntity(AuditLog entity) {
        return AuditLogDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .operation(entity.getOperation())
                .resourceType(entity.getResourceType())
                .resourceId(entity.getResourceId())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
