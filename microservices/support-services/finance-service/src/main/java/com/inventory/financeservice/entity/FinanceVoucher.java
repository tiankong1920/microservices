package com.inventory.financeservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Finance Voucher Entity.
 */
@Entity
@Table(name = "finance_vouchers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String voucherNumber;
    private LocalDate voucherDate;
    private String description;
    private BigDecimal amount;
    private String voucherType;
    private String status;
    private String referenceNumber;
    @Builder.Default
    private boolean active = true;

}
