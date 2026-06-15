package com.inventory.financeservice.service.impl;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.financeservice.dto.FinanceVoucherDTO;
import com.inventory.financeservice.entity.FinanceVoucher;
import com.inventory.financeservice.repository.IFinanceVoucherRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class FinanceVoucherServiceImplTest {

    @Mock
    private IFinanceVoucherRepository mockFinanceVoucherRepository;

    @Mock
    private ModelMapper mockModelMapper;

    private FinanceVoucherServiceImpl financeVoucherService;

    private FinanceVoucher testVoucher;
    private FinanceVoucherDTO testVoucherDTO;

    @BeforeEach
    void setUp() {
        financeVoucherService = new FinanceVoucherServiceImpl(mockFinanceVoucherRepository, mockModelMapper);
        testVoucher = FinanceVoucher.builder()
                .id(1L)
                .voucherNumber("FV-2024-001")
                .amount(BigDecimal.valueOf(1000.00))
                .status("ACTIVE")
                .build();

        testVoucherDTO = new FinanceVoucherDTO();
        testVoucherDTO.setId(1L);
        testVoucherDTO.setVoucherNumber("FV-2024-001");
        testVoucherDTO.setAmount(BigDecimal.valueOf(1000.00));
        testVoucherDTO.setStatus("ACTIVE");
    }

    @Test
    void testGetAllFinanceVouchers() {
        List<FinanceVoucher> vouchers = Arrays.asList(testVoucher);
        when(mockFinanceVoucherRepository.findAll()).thenReturn(vouchers);
        when(mockModelMapper.map(testVoucher, FinanceVoucherDTO.class)).thenReturn(testVoucherDTO);

        List<FinanceVoucherDTO> result = financeVoucherService.getAllFinanceVouchers();

        verify(mockFinanceVoucherRepository).findAll();
    }

    @Test
    void testGetFinanceVoucherById() {
        when(mockFinanceVoucherRepository.findById(1L)).thenReturn(Optional.of(testVoucher));
        when(mockModelMapper.map(testVoucher, FinanceVoucherDTO.class)).thenReturn(testVoucherDTO);

        FinanceVoucherDTO result = financeVoucherService.getFinanceVoucherById(1L);

        verify(mockFinanceVoucherRepository).findById(1L);
    }

    @Test
    void testGetFinanceVoucherByIdNotFound() {
        when(mockFinanceVoucherRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            financeVoucherService.getFinanceVoucherById(999L);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(mockFinanceVoucherRepository).findById(999L);
    }

    @Test
    void testCreateFinanceVoucher() {
        when(mockModelMapper.map(testVoucherDTO, FinanceVoucher.class)).thenReturn(testVoucher);
        when(mockFinanceVoucherRepository.save(testVoucher)).thenReturn(testVoucher);
        when(mockModelMapper.map(testVoucher, FinanceVoucherDTO.class)).thenReturn(testVoucherDTO);

        FinanceVoucherDTO result = financeVoucherService.createFinanceVoucher(testVoucherDTO);

        verify(mockFinanceVoucherRepository).save(testVoucher);
    }

    @Test
    void testUpdateFinanceVoucher() {
        when(mockFinanceVoucherRepository.findById(1L)).thenReturn(Optional.of(testVoucher));
        doNothing().when(mockModelMapper).map(any(FinanceVoucherDTO.class), any(FinanceVoucher.class));
        when(mockFinanceVoucherRepository.save(any(FinanceVoucher.class))).thenReturn(testVoucher);
        when(mockModelMapper.map(any(FinanceVoucher.class), eq(FinanceVoucherDTO.class))).thenReturn(testVoucherDTO);

        FinanceVoucherDTO result = financeVoucherService.updateFinanceVoucher(1L, testVoucherDTO);

        verify(mockFinanceVoucherRepository).findById(1L);
        verify(mockFinanceVoucherRepository).save(any(FinanceVoucher.class));
    }

    @Test
    void testUpdateFinanceVoucherNotFound() {
        when(mockFinanceVoucherRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            financeVoucherService.updateFinanceVoucher(999L, testVoucherDTO);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(mockFinanceVoucherRepository).findById(999L);
    }

    @Test
    void testDeleteFinanceVoucher() {
        financeVoucherService.deleteFinanceVoucher(1L);

        verify(mockFinanceVoucherRepository).deleteById(1L);
    }
}
