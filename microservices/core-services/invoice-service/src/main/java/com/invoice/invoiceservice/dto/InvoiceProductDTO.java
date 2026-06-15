package com.invoice.invoiceservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class InvoiceProductDTO {

    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200个字符")
    private String productName;

    @Size(max = 200, message = "规格型号长度不能超过200个字符")
    private String specification;

    @NotBlank(message = "计量单位不能为空")
    @Size(max = 50, message = "计量单位长度不能超过50个字符")
    private String unitName;

    @NotNull(message = "单价不能为空")
    @PositiveOrZero(message = "单价不能为负数")
    private BigDecimal unitPrice;

    @NotNull(message = "税率不能为空")
    @Positive(message = "税率必须为正数")
    private BigDecimal taxRate;

    @Size(max = 100, message = "商品分类长度不能超过100个字符")
    private String productCategory;

    @Size(max = 50, message = "税收分类编码长度不能超过50个字符")
    private String taxCategoryCode;

    private String status;

    private Integer usageCount;

    private java.time.LocalDateTime lastUsedAt;

    private String tenantId;

    private String createdBy;

    private String updatedBy;

    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime updatedAt;
}
