package com.inventory.salesservice.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.salesservice.dto.SalesReturnOrderDTO;
import com.inventory.salesservice.entity.SalesReturnOrder;
import com.inventory.salesservice.repository.ISalesReturnItemRepository;
import com.inventory.salesservice.repository.ISalesReturnOrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class SalesReturnServiceImplTest {

    @Mock
    private ISalesReturnOrderRepository salesReturnOrderRepository;

    @Mock
    private ISalesReturnItemRepository salesReturnItemRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SalesReturnServiceImpl salesReturnService;

    private SalesReturnOrder testSalesReturnOrder;
    private SalesReturnOrderDTO testSalesReturnOrderDTO;

    @BeforeEach
    void setUp() {
        testSalesReturnOrder = new SalesReturnOrder();
        testSalesReturnOrder.setId(1L);
        testSalesReturnOrder.setReturnNumber("RTN-2024-001");
        testSalesReturnOrder.setOriginalOrderId(100L);
        testSalesReturnOrder.setCustomerId(200L);
        testSalesReturnOrder.setWarehouseId(300L);
        testSalesReturnOrder.setReturnDate(LocalDateTime.now());
        testSalesReturnOrder.setStatus("PENDING");
        testSalesReturnOrder.setTotalAmount(BigDecimal.valueOf(100.00));

        testSalesReturnOrderDTO = new SalesReturnOrderDTO();
        testSalesReturnOrderDTO.setId(1L);
        testSalesReturnOrderDTO.setReturnNumber("RTN-2024-001");
        testSalesReturnOrderDTO.setOriginalOrderId(100L);
        testSalesReturnOrderDTO.setCustomerId(200L);
        testSalesReturnOrderDTO.setWarehouseId(300L);
        testSalesReturnOrderDTO.setStatus("PENDING");
        testSalesReturnOrderDTO.setTotalAmount(BigDecimal.valueOf(100.00));
    }

    @Test
    void testGetAllSalesReturnOrders() {
        List<SalesReturnOrder> orders = Arrays.asList(testSalesReturnOrder);
        when(salesReturnOrderRepository.findAll()).thenReturn(orders);
        when(modelMapper.map(any(SalesReturnOrder.class), eq(SalesReturnOrderDTO.class))).thenReturn(testSalesReturnOrderDTO);
        when(salesReturnItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.emptyList());

        List<SalesReturnOrderDTO> result = salesReturnService.getAllSalesReturnOrders();

        verify(salesReturnOrderRepository).findAll();
    }

    @Test
    void testGetSalesReturnOrderById() {
        when(salesReturnOrderRepository.findById(1L)).thenReturn(Optional.of(testSalesReturnOrder));
        when(modelMapper.map(any(SalesReturnOrder.class), eq(SalesReturnOrderDTO.class))).thenReturn(testSalesReturnOrderDTO);
        when(salesReturnItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.emptyList());

        SalesReturnOrderDTO result = salesReturnService.getSalesReturnOrderById(1L);

        verify(salesReturnOrderRepository).findById(1L);
    }

    @Test
    void testGetSalesReturnOrderByIdNotFound() {
        when(salesReturnOrderRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            salesReturnService.getSalesReturnOrderById(999L);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(salesReturnOrderRepository).findById(999L);
    }

    @Test
    void testGetSalesReturnOrderByReturnNumber() {
        when(salesReturnOrderRepository.findByReturnNumber("RTN-2024-001")).thenReturn(Optional.of(testSalesReturnOrder));
        when(modelMapper.map(any(SalesReturnOrder.class), eq(SalesReturnOrderDTO.class))).thenReturn(testSalesReturnOrderDTO);
        when(salesReturnItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.emptyList());

        SalesReturnOrderDTO result = salesReturnService.getSalesReturnOrderByReturnNumber("RTN-2024-001");

        verify(salesReturnOrderRepository).findByReturnNumber("RTN-2024-001");
    }

    @Test
    void testGetSalesReturnOrdersByOriginalOrderId() {
        List<SalesReturnOrder> orders = Arrays.asList(testSalesReturnOrder);
        when(salesReturnOrderRepository.findByOriginalOrderId(100L)).thenReturn(orders);
        when(modelMapper.map(any(SalesReturnOrder.class), eq(SalesReturnOrderDTO.class))).thenReturn(testSalesReturnOrderDTO);
        when(salesReturnItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.emptyList());

        List<SalesReturnOrderDTO> result = salesReturnService.getSalesReturnOrdersByOriginalOrderId(100L);

        verify(salesReturnOrderRepository).findByOriginalOrderId(100L);
    }

    @Test
    void testGetSalesReturnOrdersByCustomerId() {
        List<SalesReturnOrder> orders = Arrays.asList(testSalesReturnOrder);
        when(salesReturnOrderRepository.findByCustomerId(200L)).thenReturn(orders);
        when(modelMapper.map(any(SalesReturnOrder.class), eq(SalesReturnOrderDTO.class))).thenReturn(testSalesReturnOrderDTO);
        when(salesReturnItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.emptyList());

        List<SalesReturnOrderDTO> result = salesReturnService.getSalesReturnOrdersByCustomerId(200L);

        verify(salesReturnOrderRepository).findByCustomerId(200L);
    }

    @Test
    void testGetSalesReturnOrdersByStatus() {
        List<SalesReturnOrder> orders = Arrays.asList(testSalesReturnOrder);
        when(salesReturnOrderRepository.findByStatus("PENDING")).thenReturn(orders);
        when(modelMapper.map(any(SalesReturnOrder.class), eq(SalesReturnOrderDTO.class))).thenReturn(testSalesReturnOrderDTO);
        when(salesReturnItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.emptyList());

        List<SalesReturnOrderDTO> result = salesReturnService.getSalesReturnOrdersByStatus("PENDING");

        verify(salesReturnOrderRepository).findByStatus("PENDING");
    }

    @Test
    void testCreateSalesReturnOrder() {
        when(modelMapper.map(any(SalesReturnOrderDTO.class), eq(SalesReturnOrder.class))).thenReturn(testSalesReturnOrder);
        when(salesReturnOrderRepository.save(any(SalesReturnOrder.class))).thenReturn(testSalesReturnOrder);
        when(modelMapper.map(any(SalesReturnOrder.class), eq(SalesReturnOrderDTO.class))).thenReturn(testSalesReturnOrderDTO);
        when(salesReturnItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.emptyList());

        SalesReturnOrderDTO result = salesReturnService.createSalesReturnOrder(testSalesReturnOrderDTO);

        verify(salesReturnOrderRepository).save(any(SalesReturnOrder.class));
    }

    @Test
    void testUpdateSalesReturnOrder() {
        when(salesReturnOrderRepository.findById(1L)).thenReturn(Optional.of(testSalesReturnOrder));
        when(salesReturnOrderRepository.save(any(SalesReturnOrder.class))).thenReturn(testSalesReturnOrder);
        doNothing().when(modelMapper).map(any(SalesReturnOrderDTO.class), any(SalesReturnOrder.class));
        when(modelMapper.map(any(SalesReturnOrder.class), eq(SalesReturnOrderDTO.class))).thenReturn(testSalesReturnOrderDTO);
        when(salesReturnItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.emptyList());

        SalesReturnOrderDTO result = salesReturnService.updateSalesReturnOrder(1L, testSalesReturnOrderDTO);

        verify(salesReturnOrderRepository).findById(1L);
        verify(salesReturnOrderRepository).save(any(SalesReturnOrder.class));
    }

    @Test
    void testDeleteSalesReturnOrder() {
        when(salesReturnOrderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(salesReturnItemRepository).deleteByReturnOrderId(1L);
        doNothing().when(salesReturnOrderRepository).deleteById(1L);

        salesReturnService.deleteSalesReturnOrder(1L);

        verify(salesReturnOrderRepository).existsById(1L);
        verify(salesReturnItemRepository).deleteByReturnOrderId(1L);
        verify(salesReturnOrderRepository).deleteById(1L);
    }

    @Test
    void testUpdateSalesReturnOrderStatus() {
        when(salesReturnOrderRepository.findById(1L)).thenReturn(Optional.of(testSalesReturnOrder));
        when(salesReturnOrderRepository.save(any(SalesReturnOrder.class))).thenReturn(testSalesReturnOrder);
        when(modelMapper.map(any(SalesReturnOrder.class), eq(SalesReturnOrderDTO.class))).thenReturn(testSalesReturnOrderDTO);
        when(salesReturnItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.emptyList());

        SalesReturnOrderDTO result = salesReturnService.updateSalesReturnOrderStatus(1L, "APPROVED");

        verify(salesReturnOrderRepository).findById(1L);
        verify(salesReturnOrderRepository).save(any(SalesReturnOrder.class));
    }
}
