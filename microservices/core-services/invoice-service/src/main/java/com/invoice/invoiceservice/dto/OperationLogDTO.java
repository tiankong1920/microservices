package com.invoice.invoiceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLogDTO {

    private Long id;
    private String entityType;
    private Long entityId;
    private String operationType;
    private String operator;
    private String ipAddress;
    private String oldValue;
    private String newValue;
    private String description;
    private String tenantId;
    private java.time.LocalDateTime createdAt;
}
