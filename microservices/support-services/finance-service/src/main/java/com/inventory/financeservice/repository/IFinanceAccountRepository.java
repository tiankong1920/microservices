package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.FinanceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFinanceAccountRepository extends JpaRepository<FinanceAccount, Long> {

    Optional<FinanceAccount> findByAccountCode(String accountCode);

    List<FinanceAccount> findByAccountType(String accountType);

    List<FinanceAccount> findByStatus(String status);
}
