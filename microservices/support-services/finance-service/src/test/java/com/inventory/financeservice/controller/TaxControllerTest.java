package com.inventory.financeservice.controller;

import com.inventory.financeservice.dto.TaxRecordDTO;
import com.inventory.financeservice.service.ITaxCalculationService;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class TaxControllerTest {

    @Mock
    private ITaxCalculationService taxCalculationService;

    @InjectMocks
    private TaxController taxController;

    @Test
    void testCalculateVat() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 31, 23, 59);
        TaxRecordDTO rec = new TaxRecordDTO();
        rec.setId(1L);
        rec.setTaxType("VAT");
        when(taxCalculationService.calculateVat(
                any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(rec);

        ResponseEntity<TaxRecordDTO> response = taxController.calculateVat(
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(2), start, end);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCalculateCorporateIncomeTax() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);
        TaxRecordDTO rec = new TaxRecordDTO();
        rec.setTaxType("CIT");
        when(taxCalculationService.calculateCorporateIncomeTax(
                any(BigDecimal.class), any(BigDecimal.class),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(rec);

        ResponseEntity<TaxRecordDTO> response = taxController.calculateCorporateIncomeTax(
                BigDecimal.valueOf(1000), BigDecimal.valueOf(400), start, end);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testCalculatePersonalIncomeTax() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 31, 23, 59);
        TaxRecordDTO rec = new TaxRecordDTO();
        rec.setTaxType("PIT");
        when(taxCalculationService.calculatePersonalIncomeTax(
                any(BigDecimal.class), eq("salary"),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(rec);

        ResponseEntity<TaxRecordDTO> response = taxController.calculatePersonalIncomeTax(
                BigDecimal.valueOf(10000), "salary", start, end);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testCreateTaxRecord() {
        TaxRecordDTO rec = new TaxRecordDTO();
        when(taxCalculationService.createTaxRecord(any(TaxRecordDTO.class))).thenReturn(rec);

        ResponseEntity<TaxRecordDTO> response = taxController.createTaxRecord(rec);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testGetTaxRecordById() {
        TaxRecordDTO rec = new TaxRecordDTO();
        rec.setId(1L);
        when(taxCalculationService.getTaxRecordById(1L)).thenReturn(Optional.of(rec));

        ResponseEntity<TaxRecordDTO> response = taxController.getTaxRecordById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetAllTaxRecords() {
        when(taxCalculationService.getAllTaxRecords()).thenReturn(List.of(new TaxRecordDTO()));

        ResponseEntity<List<TaxRecordDTO>> response = taxController.getAllTaxRecords();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetTaxRecordsByType() {
        when(taxCalculationService.getTaxRecordsByType("VAT")).thenReturn(List.of());

        ResponseEntity<List<TaxRecordDTO>> response = taxController.getTaxRecordsByType("VAT");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetTaxRecordsByStatus() {
        when(taxCalculationService.getTaxRecordsByStatus("PENDING")).thenReturn(List.of());

        ResponseEntity<List<TaxRecordDTO>> response = taxController.getTaxRecordsByStatus("PENDING");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetOverdueTaxes() {
        when(taxCalculationService.getOverdueTaxes()).thenReturn(List.of());

        ResponseEntity<List<TaxRecordDTO>> response = taxController.getOverdueTaxes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetTotalTaxLiability() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);
        when(taxCalculationService.getTotalTaxLiability(start, end)).thenReturn(BigDecimal.valueOf(50000));

        ResponseEntity<BigDecimal> response = taxController.getTotalTaxLiability(start, end);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(50000), response.getBody());
    }

    @Test
    void testDeclareTax() {
        TaxRecordDTO rec = new TaxRecordDTO();
        rec.setId(1L);
        when(taxCalculationService.declareTax(1L, "u1")).thenReturn(rec);

        ResponseEntity<TaxRecordDTO> response = taxController.declareTax(1L, "u1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testPayTax() {
        TaxRecordDTO rec = new TaxRecordDTO();
        when(taxCalculationService.payTax(1L, "payer1")).thenReturn(rec);

        ResponseEntity<TaxRecordDTO> response = taxController.payTax(1L, "payer1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAdjustTax() {
        TaxRecordDTO rec = new TaxRecordDTO();
        when(taxCalculationService.adjustTax(eq(1L), any(TaxRecordDTO.class))).thenReturn(rec);

        ResponseEntity<TaxRecordDTO> response = taxController.adjustTax(1L, rec);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
