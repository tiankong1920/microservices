package com.inventory.financeservice.repository;

import com.inventory.financeservice.entity.FinanceVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFinanceVoucherRepository extends JpaRepository<FinanceVoucher, Long> {

}
