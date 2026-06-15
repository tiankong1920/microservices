package com.inventory.salesservice.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.salesservice.dto.RetailOrderDTO;
import com.inventory.salesservice.dto.RetailOrderItemDTO;
import com.inventory.salesservice.entity.RetailOrder;
import com.inventory.salesservice.entity.RetailOrderItem;
import com.inventory.salesservice.repository.IRetailOrderItemRepository;
import com.inventory.salesservice.repository.IRetailOrderRepository;
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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RetailServiceImplTest {

    @Mock
    private IRetailOrderRepository retailOrderRepository;

    @Mock
    private IRetailOrderItemRepository retailOrderItemRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RetailServiceImpl retailService;

    private RetailOrder testRetailOrder;
    private RetailOrderDTO testRetailOrderDTO;
    private RetailOrderItem testRetailOrderItem;
    private RetailOrderItemDTO testRetailOrderItemDTO;

    @BeforeEach
    void setUp() {
        testRetailOrder = RetailOrder.builder()
                .id(1L)
                .retailNumber("RTL-2024-001")
                .customerId(100L)
                .warehouseId(200L)
                .retailDate(LocalDateTime.now())
                .paymentStatus("UNPAID")
                .subtotal(BigDecimal.valueOf(100.00))
                .totalAmount(BigDecimal.valueOf(110.00))
                .build();

        testRetailOrderDTO = RetailOrderDTO.builder()
                .id(1L)
                .retailNumber("RTL-2024-001")
                .customerId(100L)
                .warehouseId(200L)
                .paymentStatus("UNPAID")
                .subtotal(BigDecimal.valueOf(100.00))
                .totalAmount(BigDecimal.valueOf(110.00))
                .build();

        testRetailOrderItem = RetailOrderItem.builder()
                .id(1L)
                .retailOrderId(1L)
                .productId(300L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .discount(BigDecimal.ZERO)
                .subtotal(BigDecimal.valueOf(100.00))
                .build();

        testRetailOrderItemDTO = RetailOrderItemDTO.builder()
                .id(1L)
                .productId(300L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .discount(BigDecimal.ZERO)
                .subtotal(BigDecimal.valueOf(100.00))
                .build();
    }

    @Test
    void testGetAllRetailOrders() {
        List<RetailOrder> orders = Arrays.asList(testRetailOrder);
        when(retailOrderRepository.findAll()).thenReturn(orders);
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(1L)).thenReturn(Collections.emptyList());

        List<RetailOrderDTO> result = retailService.getAllRetailOrders();

        verify(retailOrderRepository).findAll();
    }

    @Test
    void testGetRetailOrderById() {
        when(retailOrderRepository.findById(1L)).thenReturn(Optional.of(testRetailOrder));
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(1L)).thenReturn(Collections.emptyList());

        RetailOrderDTO result = retailService.getRetailOrderById(1L);

        verify(retailOrderRepository).findById(1L);
    }

    @Test
    void testGetRetailOrderByIdNotFound() {
        when(retailOrderRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            retailService.getRetailOrderById(999L);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(retailOrderRepository).findById(999L);
    }

    @Test
    void testGetRetailOrderByRetailNumber() {
        when(retailOrderRepository.findByRetailNumber("RTL-2024-001")).thenReturn(Optional.of(testRetailOrder));
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(1L)).thenReturn(Collections.emptyList());

        RetailOrderDTO result = retailService.getRetailOrderByRetailNumber("RTL-2024-001");

        verify(retailOrderRepository).findByRetailNumber("RTL-2024-001");
    }

    @Test
    void testGetRetailOrdersByCustomerId() {
        List<RetailOrder> orders = Arrays.asList(testRetailOrder);
        when(retailOrderRepository.findByCustomerId(100L)).thenReturn(orders);
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(1L)).thenReturn(Collections.emptyList());

        List<RetailOrderDTO> result = retailService.getRetailOrdersByCustomerId(100L);

        verify(retailOrderRepository).findByCustomerId(100L);
    }

    @Test
    void testGetRetailOrdersByWarehouseId() {
        List<RetailOrder> orders = Arrays.asList(testRetailOrder);
        when(retailOrderRepository.findByWarehouseId(200L)).thenReturn(orders);
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(1L)).thenReturn(Collections.emptyList());

        List<RetailOrderDTO> result = retailService.getRetailOrdersByWarehouseId(200L);

        verify(retailOrderRepository).findByWarehouseId(200L);
    }

    @Test
    void testGetRetailOrdersByPaymentStatus() {
        List<RetailOrder> orders = Arrays.asList(testRetailOrder);
        when(retailOrderRepository.findByPaymentStatus("UNPAID")).thenReturn(orders);
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(1L)).thenReturn(Collections.emptyList());

        List<RetailOrderDTO> result = retailService.getRetailOrdersByPaymentStatus("UNPAID");

        verify(retailOrderRepository).findByPaymentStatus("UNPAID");
    }

    @Test
    void testGetRetailOrdersByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<RetailOrder> orders = Arrays.asList(testRetailOrder);
        when(retailOrderRepository.findByRetailDateBetween(startDate, endDate)).thenReturn(orders);
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(1L)).thenReturn(Collections.emptyList());

        List<RetailOrderDTO> result = retailService.getRetailOrdersByDateRange(startDate, endDate);

        verify(retailOrderRepository).findByRetailDateBetween(startDate, endDate);
    }

    @Test
    void testCreateRetailOrder() {
        when(modelMapper.map(any(RetailOrderDTO.class), eq(RetailOrder.class))).thenReturn(testRetailOrder);
        when(retailOrderRepository.save(any(RetailOrder.class))).thenReturn(testRetailOrder);
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(anyLong())).thenReturn(Collections.emptyList());

        RetailOrderDTO result = retailService.createRetailOrder(testRetailOrderDTO);

        verify(retailOrderRepository).save(any(RetailOrder.class));
    }

    @Test
    void testCreateRetailOrderWithItems() {
        testRetailOrderDTO.setItems(Arrays.asList(testRetailOrderItemDTO));
        when(modelMapper.map(any(RetailOrderDTO.class), eq(RetailOrder.class))).thenReturn(testRetailOrder);
        when(retailOrderRepository.save(any(RetailOrder.class))).thenReturn(testRetailOrder);
        when(modelMapper.map(any(RetailOrderItemDTO.class), eq(RetailOrderItem.class))).thenReturn(testRetailOrderItem);
        when(retailOrderItemRepository.save(any(RetailOrderItem.class))).thenReturn(testRetailOrderItem);
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(anyLong())).thenReturn(Arrays.asList(testRetailOrderItem));
        when(modelMapper.map(any(RetailOrderItem.class), eq(RetailOrderItemDTO.class))).thenReturn(testRetailOrderItemDTO);

        RetailOrderDTO result = retailService.createRetailOrder(testRetailOrderDTO);

        verify(retailOrderRepository, times(2)).save(any(RetailOrder.class));
        verify(retailOrderItemRepository).save(any(RetailOrderItem.class));
    }

    @Test
    void testUpdateRetailOrder() {
        when(retailOrderRepository.findById(1L)).thenReturn(Optional.of(testRetailOrder));
        when(retailOrderRepository.save(any(RetailOrder.class))).thenReturn(testRetailOrder);
        doNothing().when(modelMapper).map(any(RetailOrderDTO.class), any(RetailOrder.class));
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(1L)).thenReturn(Collections.emptyList());

        RetailOrderDTO result = retailService.updateRetailOrder(1L, testRetailOrderDTO);

        verify(retailOrderRepository).findById(1L);
        verify(retailOrderRepository).save(any(RetailOrder.class));
    }

    @Test
    void testDeleteRetailOrder() {
        when(retailOrderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(retailOrderItemRepository).deleteByRetailOrderId(1L);
        doNothing().when(retailOrderRepository).deleteById(1L);

        retailService.deleteRetailOrder(1L);

        verify(retailOrderRepository).existsById(1L);
        verify(retailOrderItemRepository).deleteByRetailOrderId(1L);
        verify(retailOrderRepository).deleteById(1L);
    }

    @Test
    void testDeleteRetailOrderNotFound() {
        when(retailOrderRepository.existsById(999L)).thenReturn(false);

        try {
            retailService.deleteRetailOrder(999L);
        } catch (IllegalStateException e) {
            // Expected
        }

        verify(retailOrderRepository).existsById(999L);
        verify(retailOrderItemRepository, never()).deleteByRetailOrderId(any());
        verify(retailOrderRepository, never()).deleteById(any());
    }

    @Test
    void testUpdateRetailOrderPaymentStatus() {
        when(retailOrderRepository.findById(1L)).thenReturn(Optional.of(testRetailOrder));
        when(retailOrderRepository.save(any(RetailOrder.class))).thenReturn(testRetailOrder);
        when(modelMapper.map(any(RetailOrder.class), eq(RetailOrderDTO.class))).thenReturn(testRetailOrderDTO);
        when(retailOrderItemRepository.findByRetailOrderId(1L)).thenReturn(Collections.emptyList());

        RetailOrderDTO result = retailService.updateRetailOrderPaymentStatus(1L, "PAID");

        verify(retailOrderRepository).findById(1L);
        verify(retailOrderRepository).save(any(RetailOrder.class));
    }

    @Test
    void testGetTotalRetailByCustomerId() {
        List<RetailOrder> orders = Arrays.asList(testRetailOrder);
        when(retailOrderRepository.findByCustomerId(100L)).thenReturn(orders);

        Double total = retailService.getTotalRetailByCustomerId(100L);

        verify(retailOrderRepository).findByCustomerId(100L);
    }
}
