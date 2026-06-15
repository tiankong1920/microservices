package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.SettlementAccountDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ISettlementAccountService {

    SettlementAccountDTO createSettlementAccount(SettlementAccountDTO settlementAccountDTO);

    SettlementAccountDTO getSettlementAccountById(Long id);

    List<SettlementAccountDTO> getAllSettlementAccounts();

    SettlementAccountDTO updateSettlementAccount(Long id, SettlementAccountDTO settlementAccountDTO);

    void deleteSettlementAccount(Long id);

    SettlementAccountDTO getDefaultSettlementAccount();

    SettlementAccountDTO createAccount(SettlementAccountDTO accountDTO);

    SettlementAccountDTO updateAccount(Long id, SettlementAccountDTO accountDTO);

    Optional<SettlementAccountDTO> getAccountById(Long id);

    Optional<SettlementAccountDTO> getAccountByAccountNumber(String accountNumber);

    List<SettlementAccountDTO> getAllAccounts();

    List<SettlementAccountDTO> getAccountsByType(String accountType);

    List<SettlementAccountDTO> getActiveAccounts();

    List<SettlementAccountDTO> getAccountsByBank(String bankName);

    SettlementAccountDTO enableAccount(Long id);

    SettlementAccountDTO disableAccount(Long id);

    SettlementAccountDTO updateBalance(Long id, BigDecimal amount, String transactionType);

    BigDecimal getAccountBalance(Long id);

    List<SettlementAccountDTO> getAccountsBelowMinimumBalance(BigDecimal minimumBalance);

    SettlementAccountDTO transferBetweenAccounts(Long fromAccountId, Long toAccountId, BigDecimal amount, String description);

    List<SettlementAccountDTO> searchAccounts(String keyword);

    byte[] exportAccountsToExcel();

    byte[] exportAccountsToCsv();

    class TransferResult {
        private boolean success;
        private String fromAccountNumber;
        private String toAccountNumber;
        private BigDecimal amount;
        private String transactionId;
        private String errorMessage;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getFromAccountNumber() { return fromAccountNumber; }
        public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }
        public String getToAccountNumber() { return toAccountNumber; }
        public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
