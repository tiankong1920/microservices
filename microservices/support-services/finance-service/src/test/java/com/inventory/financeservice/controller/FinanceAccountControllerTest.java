package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.ExpenseDTO;
import com.inventory.financeservice.dto.IncomeDTO;
import com.inventory.financeservice.dto.SettlementAccountDTO;
import com.inventory.financeservice.service.IFinanceAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class FinanceAccountControllerTest {

    @Mock
    private IFinanceAccountService financeAccountService;

    @InjectMocks
    private FinanceAccountController financeAccountController;

    @Test
    void testGetAllIncomes() {
        when(financeAccountService.getAllIncomes()).thenReturn(List.of(new IncomeDTO()));

        ResponseEntity<List<IncomeDTO>> response = financeAccountController.getAllIncomes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetIncomeById() {
        IncomeDTO dto = new IncomeDTO();
        dto.setId(1L);
        when(financeAccountService.getIncomeById(1L)).thenReturn(dto);

        ResponseEntity<IncomeDTO> response = financeAccountController.getIncomeById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetIncomesByDateRange() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 31, 23, 59);
        when(financeAccountService.getIncomesByDateRange(start, end)).thenReturn(List.of());

        ResponseEntity<List<IncomeDTO>> response = financeAccountController.getIncomesByDateRange(start, end);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCreateIncome() {
        IncomeDTO dto = new IncomeDTO();
        when(financeAccountService.createIncome(any(IncomeDTO.class))).thenReturn(dto);

        ResponseEntity<IncomeDTO> response = financeAccountController.createIncome(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testUpdateIncome() {
        IncomeDTO dto = new IncomeDTO();
        when(financeAccountService.updateIncome(eq(1L), any(IncomeDTO.class))).thenReturn(dto);

        ResponseEntity<IncomeDTO> response = financeAccountController.updateIncome(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteIncome() {
        doNothing().when(financeAccountService).deleteIncome(1L);

        ResponseEntity<Void> response = financeAccountController.deleteIncome(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(financeAccountService).deleteIncome(1L);
    }

    @Test
    void testGetAllExpenses() {
        when(financeAccountService.getAllExpenses()).thenReturn(List.of(new ExpenseDTO()));

        ResponseEntity<List<ExpenseDTO>> response = financeAccountController.getAllExpenses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetExpenseById() {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(1L);
        when(financeAccountService.getExpenseById(1L)).thenReturn(dto);

        ResponseEntity<ExpenseDTO> response = financeAccountController.getExpenseById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCreateExpense() {
        ExpenseDTO dto = new ExpenseDTO();
        when(financeAccountService.createExpense(any(ExpenseDTO.class))).thenReturn(dto);

        ResponseEntity<ExpenseDTO> response = financeAccountController.createExpense(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testDeleteExpense() {
        doNothing().when(financeAccountService).deleteExpense(1L);

        ResponseEntity<Void> response = financeAccountController.deleteExpense(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetAllSettlementAccounts() {
        when(financeAccountService.getAllSettlementAccounts()).thenReturn(List.of(new SettlementAccountDTO()));

        ResponseEntity<List<SettlementAccountDTO>> response = financeAccountController.getAllSettlementAccounts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetSettlementAccountById() {
        SettlementAccountDTO dto = new SettlementAccountDTO();
        dto.setId(1L);
        when(financeAccountService.getSettlementAccountById(1L)).thenReturn(dto);

        ResponseEntity<SettlementAccountDTO> response = financeAccountController.getSettlementAccountById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCreateSettlementAccount() {
        SettlementAccountDTO dto = new SettlementAccountDTO();
        when(financeAccountService.createSettlementAccount(any(SettlementAccountDTO.class))).thenReturn(dto);

        ResponseEntity<SettlementAccountDTO> response = financeAccountController.createSettlementAccount(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testAdjustAccountBalance() {
        SettlementAccountDTO dto = new SettlementAccountDTO();
        when(financeAccountService.adjustAccountBalance(eq(1L), any(BigDecimal.class))).thenReturn(dto);

        ResponseEntity<SettlementAccountDTO> response =
                financeAccountController.adjustAccountBalance(1L, BigDecimal.valueOf(1000));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
