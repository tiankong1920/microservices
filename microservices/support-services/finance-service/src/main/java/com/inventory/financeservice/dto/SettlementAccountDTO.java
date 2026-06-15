package com.inventory.financeservice.dto;

import com.inventory.financeservice.entity.SettlementAccount;
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
public class SettlementAccountDTO {

    private Long id;
    private String accountNumber;
    private String accountName;
    private String accountType;
    private String bankName;
    private String bankBranch;
    private String accountHolder;
    private BigDecimal balance;
    private String currency;
    private BigDecimal minimumBalance;
    private BigDecimal availableBalance;
    private Boolean isActive;
    private String status;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public static SettlementAccountDTO fromEntity(SettlementAccount account) {
        if (account == null) return null;

        return SettlementAccountDTO.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .bankName(account.getBankName())
                .bankBranch(account.getBankBranch())
                .accountHolder(account.getAccountHolder())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .minimumBalance(account.getMinimumBalance())
                .availableBalance(account.getAvailableBalance())
                .isActive(account.getIsActive())
                .status(account.getStatus())
                .description(account.getDescription())
                .createdBy(account.getCreatedBy())
                .createdAt(account.getCreatedAt())
                .updatedBy(account.getUpdatedBy())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public SettlementAccount toEntity() {
        return SettlementAccount.builder()
                .id(this.id)
                .accountNumber(this.accountNumber)
                .accountName(this.accountName)
                .accountType(this.accountType)
                .bankName(this.bankName)
                .bankBranch(this.bankBranch)
                .accountHolder(this.accountHolder)
                .balance(this.balance != null ? this.balance : BigDecimal.ZERO)
                .currency(this.currency != null ? this.currency : "CNY")
                .minimumBalance(this.minimumBalance != null ? this.minimumBalance : BigDecimal.ZERO)
                .availableBalance(this.availableBalance != null ? this.availableBalance : BigDecimal.ZERO)
                .isActive(this.isActive != null ? this.isActive : true)
                .status(this.status != null ? this.status : "ACTIVE")
                .description(this.description)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .build();
    }

    public static final String TYPE_CASH = "CASH";
    public static final String TYPE_BANK = "BANK";
    public static final String TYPE_CREDIT_CARD = "CREDIT_CARD";
    public static final String TYPE_THIRD_PARTY = "THIRD_PARTY";
    public static final String TYPE_VIRTUAL = "VIRTUAL";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_FROZEN = "FROZEN";
    public static final String STATUS_CLOSED = "CLOSED";
}
