package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.SettlementAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ISettlementAccountRepository extends JpaRepository<SettlementAccount, Long> {
    List<SettlementAccount> findByAccountType(String accountType);
    List<SettlementAccount> findByStatus(String status);
    SettlementAccount findByIsDefaultTrue();
    List<SettlementAccount> findByCurrency(String currency);
    Optional<SettlementAccount> findByAccountNumber(String accountNumber);
    List<SettlementAccount> findByIsActiveTrue();
    List<SettlementAccount> findByBankName(String bankName);
    List<SettlementAccount> findByAccountNumberContaining(String accountNumber);
    List<SettlementAccount> findByAccountNameContaining(String accountName);

    @Query("SELECT s FROM SettlementAccount s WHERE " +
           "(LOWER(s.accountNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.accountName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.bankName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<SettlementAccount> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT s.balance FROM SettlementAccount s WHERE s.id = :id")
    BigDecimal getBalanceById(@Param("id") Long id);
}
