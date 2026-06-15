package com.inventory.financeservice.dto;

import com.inventory.financeservice.entity.TaxRecord;
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
public class TaxRecordDTO {

    private Long id;
    private String taxNumber;
    private String taxType;
    private LocalDateTime taxablePeriodStart;
    private LocalDateTime taxablePeriodEnd;
    private BigDecimal taxableAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private String status;
    private LocalDateTime declarationDate;
    private LocalDateTime dueDate;
    private LocalDateTime paymentDate;
    private BigDecimal inputTaxAmount;
    private BigDecimal outputTaxAmount;
    private BigDecimal netTaxAmount;
    private String referenceNumber;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaxRecordDTO fromEntity(TaxRecord record) {
        if (record == null) return null;

        return TaxRecordDTO.builder()
                .id(record.getId())
                .taxNumber(record.getTaxNumber())
                .taxType(record.getTaxType() != null ? record.getTaxType().name() : null)
                .taxablePeriodStart(record.getTaxablePeriodStart())
                .taxablePeriodEnd(record.getTaxablePeriodEnd())
                .taxableAmount(record.getTaxableAmount())
                .taxRate(record.getTaxRate())
                .taxAmount(record.getTaxAmount())
                .status(record.getStatus() != null ? record.getStatus().name() : null)
                .declarationDate(record.getDeclarationDate())
                .dueDate(record.getDueDate())
                .paymentDate(record.getPaymentDate())
                .inputTaxAmount(record.getInputTaxAmount())
                .outputTaxAmount(record.getOutputTaxAmount())
                .netTaxAmount(record.getNetTaxAmount())
                .referenceNumber(record.getReferenceNumber())
                .remarks(record.getRemarks())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    public TaxRecord toEntity() {
        return TaxRecord.builder()
                .id(this.id)
                .taxNumber(this.taxNumber)
                .taxType(this.taxType != null ? TaxRecord.TaxType.valueOf(this.taxType) : null)
                .taxablePeriodStart(this.taxablePeriodStart)
                .taxablePeriodEnd(this.taxablePeriodEnd)
                .taxableAmount(this.taxableAmount)
                .taxRate(this.taxRate)
                .taxAmount(this.taxAmount)
                .status(this.status != null ? TaxRecord.TaxStatus.valueOf(this.status) : TaxRecord.TaxStatus.PENDING)
                .declarationDate(this.declarationDate)
                .dueDate(this.dueDate)
                .paymentDate(this.paymentDate)
                .inputTaxAmount(this.inputTaxAmount)
                .outputTaxAmount(this.outputTaxAmount)
                .netTaxAmount(this.netTaxAmount)
                .referenceNumber(this.referenceNumber)
                .remarks(this.remarks)
                .build();
    }
}
