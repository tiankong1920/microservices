package com.inventory.financeservice.service;

import com.inventory.financeservice.dto.IncomeDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IIncomeService {

    IncomeDTO createIncome(IncomeDTO incomeDTO);

    List<IncomeDTO> getAllIncomes();

    Optional<IncomeDTO> getIncomeById(Long id);

    Optional<IncomeDTO> findIncomeById(Long id);

    Optional<IncomeDTO> findIncomeByNumber(String incomeNumber);

    IncomeDTO updateIncome(Long id, IncomeDTO incomeDTO);

    void deleteIncome(Long id);

    List<IncomeDTO> getIncomesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<IncomeDTO> getIncomesByType(String incomeType);

    List<IncomeDTO> getIncomesByStatus(String incomeStatus);

    List<IncomeDTO> getIncomesBySettlementAccount(Long accountId);

    List<IncomeDTO> searchIncomes(String keyword);

    IncomeDTO approveIncome(Long id, String approverId);

    IncomeDTO rejectIncome(Long id, String rejectorId, String reason);

    IncomeDTO cancelIncome(Long id);

    BigDecimal getTotalIncomeByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<IncomeDTO> getPendingApprovalIncomes();

    byte[] exportIncomesToExcel();

    byte[] exportIncomesToCsv();
}
