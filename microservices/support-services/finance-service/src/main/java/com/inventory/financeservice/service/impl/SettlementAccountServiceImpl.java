package com.inventory.financeservice.service.impl;

import com.inventory.financeservice.dto.SettlementAccountDTO;
import com.inventory.financeservice.entity.FinanceAuditLog;
import com.inventory.financeservice.entity.SettlementAccount;
import com.inventory.financeservice.repository.IFinanceAuditLogRepository;
import com.inventory.financeservice.repository.ISettlementAccountRepository;
import com.inventory.financeservice.service.ISettlementAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@Service
@Slf4j
@RequiredArgsConstructor
public class SettlementAccountServiceImpl implements ISettlementAccountService {

    private final ISettlementAccountRepository accountRepository;
    private final IFinanceAuditLogRepository auditLogRepository;

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public SettlementAccountDTO createAccount(SettlementAccountDTO accountDTO) {
        log.info("Creating settlement account: {}", accountDTO.getAccountName());

        SettlementAccount account = accountDTO.toEntity();
        if (account.getAccountNumber() == null || account.getAccountNumber().isEmpty()) {
            account.setAccountNumber(generateAccountNumber(accountDTO.getAccountType()));
        }
        account.setBalance(BigDecimal.ZERO);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setIsActive(true);
        account.setStatus(SettlementAccountDTO.STATUS_ACTIVE);

        SettlementAccount saved = accountRepository.save(account);

        createAuditLog(saved.getId(), "SettlementAccount", saved.getId(), "CREATE",
                null, saved.toString(), saved.getCreatedBy(), "Settlement account created");

        return SettlementAccountDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public SettlementAccountDTO createSettlementAccount(SettlementAccountDTO settlementAccountDTO) {
        return createAccount(settlementAccountDTO);
    }

    @Override
    public SettlementAccountDTO getSettlementAccountById(Long id) {
        return getAccountById(id).orElseThrow(() -> new RuntimeException("Settlement account not found with id: " + id));
    }

    @Override
    public List<SettlementAccountDTO> getAllSettlementAccounts() {
        return getAllAccounts();
    }

    @Override
    @Transactional
    public SettlementAccountDTO updateSettlementAccount(Long id, SettlementAccountDTO settlementAccountDTO) {
        return updateAccount(id, settlementAccountDTO);
    }

    @Override
    @Transactional
    public void deleteSettlementAccount(Long id) {
        log.info("Deleting settlement account: {}", id);
        SettlementAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));

        if (Boolean.TRUE.equals(account.getIsDefault())) {
            throw new IllegalStateException("Cannot delete default settlement account");
        }

        accountRepository.delete(account);
        createAuditLog(id, "SettlementAccount", id, "DELETE",
                account.toString(), null, "SYSTEM", "Settlement account deleted");
    }

    @Override
    public SettlementAccountDTO getDefaultSettlementAccount() {
        SettlementAccount account = accountRepository.findByIsDefaultTrue();
        if (account == null) {
            throw new IllegalStateException("No default settlement account found");
        }
        return SettlementAccountDTO.fromEntity(account);
    }

    @Override
    @Transactional
    public SettlementAccountDTO updateAccount(Long id, SettlementAccountDTO accountDTO) {
        log.info("Updating settlement account: {}", id);

        SettlementAccount existing = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));

        String oldValue = existing.toString();

        existing.setAccountName(accountDTO.getAccountName());
        existing.setAccountType(accountDTO.getAccountType());
        existing.setBankName(accountDTO.getBankName());
        existing.setBankBranch(accountDTO.getBankBranch());
        existing.setAccountHolder(accountDTO.getAccountHolder());
        existing.setCurrency(accountDTO.getCurrency());
        existing.setMinimumBalance(accountDTO.getMinimumBalance());
        existing.setDescription(accountDTO.getDescription());

        SettlementAccount updated = accountRepository.save(existing);

        createAuditLog(updated.getId(), "SettlementAccount", updated.getId(), "UPDATE",
                oldValue, updated.toString(), accountDTO.getUpdatedBy(), "Settlement account updated");

        return SettlementAccountDTO.fromEntity(updated);
    }

    @Override
    public Optional<SettlementAccountDTO> getAccountById(Long id) {
        return accountRepository.findById(id).map(SettlementAccountDTO::fromEntity);
    }

    @Override
    public Optional<SettlementAccountDTO> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber).map(SettlementAccountDTO::fromEntity);
    }

    @Override
    public List<SettlementAccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(SettlementAccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SettlementAccountDTO> getAccountsByType(String accountType) {
        return accountRepository.findByAccountType(accountType).stream()
                .map(SettlementAccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SettlementAccountDTO> getActiveAccounts() {
        return accountRepository.findByIsActiveTrue().stream()
                .map(SettlementAccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SettlementAccountDTO> getAccountsByBank(String bankName) {
        return accountRepository.findByBankName(bankName).stream()
                .map(SettlementAccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SettlementAccountDTO enableAccount(Long id) {
        SettlementAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));

        account.setIsActive(true);
        account.setStatus(SettlementAccountDTO.STATUS_ACTIVE);

        SettlementAccount updated = accountRepository.save(account);

        createAuditLog(updated.getId(), "SettlementAccount", updated.getId(), "ENABLE",
                null, updated.toString(), "SYSTEM", "Settlement account enabled");

        return SettlementAccountDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public SettlementAccountDTO disableAccount(Long id) {
        SettlementAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));

        account.setIsActive(false);
        account.setStatus(SettlementAccountDTO.STATUS_INACTIVE);

        SettlementAccount updated = accountRepository.save(account);

        createAuditLog(updated.getId(), "SettlementAccount", updated.getId(), "DISABLE",
                null, updated.toString(), "SYSTEM", "Settlement account disabled");

        return SettlementAccountDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public SettlementAccountDTO updateBalance(Long id, BigDecimal amount, String transactionType) {
        SettlementAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));

        BigDecimal oldBalance = account.getBalance();

        if ("DEPOSIT".equals(transactionType)) {
            account.setBalance(account.getBalance().add(amount));
        } else if ("WITHDRAW".equals(transactionType)) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient balance");
            }
            account.setBalance(account.getBalance().subtract(amount));
        }

        account.setAvailableBalance(account.getBalance().subtract(account.getMinimumBalance()));

        SettlementAccount updated = accountRepository.save(account);

        createAuditLog(updated.getId(), "SettlementAccount", updated.getId(), "UPDATE_BALANCE",
                oldBalance.toString(), updated.getBalance().toString(), "SYSTEM",
                "Balance updated via " + transactionType);

        return SettlementAccountDTO.fromEntity(updated);
    }

    @Override
    public BigDecimal getAccountBalance(Long id) {
        SettlementAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
        return account.getBalance();
    }

    @Override
    public List<SettlementAccountDTO> getAccountsBelowMinimumBalance(BigDecimal minimumBalance) {
        return accountRepository.findAll().stream()
                .filter(acc -> acc.getBalance().compareTo(minimumBalance) < 0)
                .map(SettlementAccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SettlementAccountDTO transferBetweenAccounts(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        log.info("Transferring {} from account {} to account {}", amount, fromAccountId, toAccountId);

        SettlementAccount fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Source account not found: " + fromAccountId));

        SettlementAccount toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Destination account not found: " + toAccountId));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in source account");
        }

        String transactionId = "TRF" + LocalDateTime.now().format(CODE_FORMATTER) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        fromAccount.setAvailableBalance(fromAccount.getBalance().subtract(fromAccount.getMinimumBalance()));

        toAccount.setBalance(toAccount.getBalance().add(amount));
        toAccount.setAvailableBalance(toAccount.getBalance().subtract(toAccount.getMinimumBalance()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        createAuditLog(fromAccount.getId(), "SettlementAccount", fromAccount.getId(), "TRANSFER_OUT",
                null, amount.toString(), "SYSTEM",
                "Transfer to " + toAccount.getAccountNumber() + ". " + description + ". Ref: " + transactionId);

        createAuditLog(toAccount.getId(), "SettlementAccount", toAccount.getId(), "TRANSFER_IN",
                null, amount.toString(), "SYSTEM",
                "Transfer from " + fromAccount.getAccountNumber() + ". " + description + ". Ref: " + transactionId);

        return SettlementAccountDTO.fromEntity(fromAccount);
    }

    @Override
    public List<SettlementAccountDTO> searchAccounts(String keyword) {
        return accountRepository.searchByKeyword(keyword).stream()
                .map(SettlementAccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportAccountsToExcel() {
        List<SettlementAccount> accounts = accountRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("Account Number,Account Name,Account Type,Bank,Branch,Holder,Balance,Currency,Status\n");
        for (SettlementAccount acc : accounts) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    acc.getAccountNumber(),
                    acc.getAccountName(),
                    acc.getAccountType(),
                    acc.getBankName(),
                    acc.getBankBranch(),
                    acc.getAccountHolder(),
                    acc.getBalance(),
                    acc.getCurrency(),
                    acc.getStatus()));
        }
        return csv.toString().getBytes();
    }

    @Override
    public byte[] exportAccountsToCsv() {
        return exportAccountsToExcel();
    }

    private String generateAccountNumber(String accountType) {
        String prefix = switch (accountType) {
            case SettlementAccountDTO.TYPE_BANK -> "BA";
            case SettlementAccountDTO.TYPE_CREDIT_CARD -> "CC";
            case SettlementAccountDTO.TYPE_THIRD_PARTY -> "TP";
            case SettlementAccountDTO.TYPE_VIRTUAL -> "VT";
            default -> "CA";
        };
        return prefix + LocalDateTime.now().format(CODE_FORMATTER) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private void createAuditLog(Long entityId, String entityType, Long refId, String operationType,
                                String oldValue, String newValue, String operatorId, String changeSummary) {
        FinanceAuditLog auditLog = FinanceAuditLog.builder()
                .entityType(entityType)
                .entityId(refId)
                .operationType(operationType)
                .operatorId(operatorId != null ? operatorId : "SYSTEM")
                .operationTime(LocalDateTime.now())
                .severity(FinanceAuditLog.Severity.INFO)
                .oldValue(oldValue)
                .newValue(newValue)
                .changeSummary(changeSummary)
                .build();

        auditLogRepository.save(auditLog);
    }
}
