package com.inventory.financeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    private Long id;

    @NotBlank(message = "付款编号不能为空")
    @Size(max = 50, message = "付款编号长度不能超过50个字符")
    private String paymentNumber;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @Size(max = 100, message = "供应商名称长度不能超过100个字符")
    private String supplierName;

    @NotNull(message = "付款日期不能为空")
    private LocalDateTime paymentDate;

    @NotNull(message = "付款金额不能为空")
    @Positive(message = "付款金额必须为正数")
    private BigDecimal paymentAmount;

    @NotBlank(message = "付款方式不能为空")
    @Size(max = 50, message = "付款方式长度不能超过50个字符")
    private String paymentMethod;

    @Size(max = 50, message = "关联单据类型长度不能超过50个字符")
    private String relatedDocumentType;

    @Size(max = 50, message = "关联单据编号长度不能超过50个字符")
    private String relatedDocumentNumber;

    private Long relatedDocumentId;

    @NotBlank(message = "付款状态不能为空")
    @Size(max = 50, message = "付款状态长度不能超过50个字符")
    private String paymentStatus;

    @Size(max = 100, message = "付款人长度不能超过100个字符")
    private String payer;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Size(max = 100, message = "创建人长度不能超过100个字符")
    private String createdBy;

    @Size(max = 100, message = "更新人长度不能超过100个字符")
    private String updatedBy;
}
