package com.inventory.financeservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tax_number", nullable = false, unique = true, length = 50)
    private String taxNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false, length = 30)
    private TaxType taxType;

    @Column(name = "taxable_period_start", nullable = false)
    private LocalDateTime taxablePeriodStart;

    @Column(name = "taxable_period_end", nullable = false)
    private LocalDateTime taxablePeriodEnd;

    @Column(name = "taxable_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxableAmount;

    @Column(name = "tax_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TaxStatus status = TaxStatus.PENDING;

    @Column(name = "declaration_date")
    private LocalDateTime declarationDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "input_tax_amount", precision = 19, scale = 4)
    private BigDecimal inputTaxAmount;

    @Column(name = "output_tax_amount", precision = 19, scale = 4)
    private BigDecimal outputTaxAmount;

    @Column(name = "net_tax_amount", precision = 19, scale = 4)
    private BigDecimal netTaxAmount;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TaxType {
        VAT_INPUT, VAT_OUTPUT, VAT_PAYABLE,
        CORPORATE_INCOME_TAX, PERSONAL_INCOME_TAX,
        CONSUMPTION_TAX, PROPERTY_TAX, OTHER
    }

    public enum TaxStatus {
        PENDING, DECLARED, PAID, OVERDUE, ADJUSTED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
