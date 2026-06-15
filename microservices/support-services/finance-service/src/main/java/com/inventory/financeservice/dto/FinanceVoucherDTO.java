package com.inventory.financeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Finance Voucher DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceVoucherDTO {

    private Long id;
    private String voucherNumber;
    private LocalDate voucherDate;
    private String description;
    private BigDecimal amount;
    private String voucherType;
    private String status;
    private String referenceNumber;
}
