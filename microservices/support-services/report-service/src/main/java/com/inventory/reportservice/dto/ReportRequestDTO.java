package com.inventory.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 报表生成请求DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
    
    @NotBlank(message = "报表名称不能为空")
    @Size(max = 100, message = "报表名称长度不能超过100个字符")
    private String reportName;
    
    @NotBlank(message = "报表类型不能为空")
    private String reportType;
    
    private String description;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private String parameters;
    
    private Long templateId;
}

