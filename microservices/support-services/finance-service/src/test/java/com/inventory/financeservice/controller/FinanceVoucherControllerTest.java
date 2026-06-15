package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.FinanceVoucherDTO;
import com.inventory.financeservice.service.IFinanceVoucherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
class FinanceVoucherControllerTest {

    @Mock
    private IFinanceVoucherService financeVoucherService;

    @InjectMocks
    private FinanceVoucherController financeVoucherController;

    private FinanceVoucherDTO testVoucher;

    @BeforeEach
    void setUp() {
        testVoucher = new FinanceVoucherDTO();
        testVoucher.setId(1L);
        testVoucher.setVoucherNumber("V-2026-0001");
    }

    @Test
    void testGetAllVouchers() {
        when(financeVoucherService.getAllFinanceVouchers()).thenReturn(List.of(testVoucher));

        ResponseEntity<List<FinanceVoucherDTO>> response = financeVoucherController.getAllVouchers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetVoucherById() {
        when(financeVoucherService.getFinanceVoucherById(1L)).thenReturn(testVoucher);

        ResponseEntity<FinanceVoucherDTO> response = financeVoucherController.getVoucherById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("V-2026-0001", response.getBody().getVoucherNumber());
    }

    @Test
    void testCreateVoucher() {
        when(financeVoucherService.createFinanceVoucher(any(FinanceVoucherDTO.class))).thenReturn(testVoucher);

        ResponseEntity<FinanceVoucherDTO> response = financeVoucherController.createVoucher(testVoucher);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateVoucher() {
        when(financeVoucherService.updateFinanceVoucher(eq(1L), any(FinanceVoucherDTO.class))).thenReturn(testVoucher);

        ResponseEntity<FinanceVoucherDTO> response = financeVoucherController.updateVoucher(1L, testVoucher);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteVoucher() {
        doNothing().when(financeVoucherService).deleteFinanceVoucher(1L);

        ResponseEntity<Void> response = financeVoucherController.deleteVoucher(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(financeVoucherService).deleteFinanceVoucher(1L);
    }
}
