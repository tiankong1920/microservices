package com.inventory.mallservice.service.impl;

import com.inventory.mallservice.entity.Order;
import com.inventory.mallservice.entity.Shipment;
import com.inventory.mallservice.repository.IOrderRepository;
import com.inventory.mallservice.repository.IShipmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("物流服务测试")
@SuppressWarnings("null")
class ShipmentServiceImplTest {

    @Mock
    private IShipmentRepository shipmentRepository;

    @Mock
    private IOrderRepository orderRepository;

    private ShipmentServiceImpl shipmentService;

    private Order testOrder;
    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        shipmentService = new ShipmentServiceImpl(shipmentRepository, orderRepository);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20240101001");

        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setOrder(testOrder);
        testShipment.setLogisticsCompany("顺丰速运");
        testShipment.setTrackingNumber("SF1234567890");
        testShipment.setStatus("PENDING");
        testShipment.setEstimatedDelivery(LocalDateTime.now().plusDays(3));
    }

    @Test
    @DisplayName("创建物流")
    void createShipment() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

        Shipment result = shipmentService.createShipment(1L, "顺丰速运", "SF1234567890");

        assertNotNull(result);
        assertEquals("顺丰速运", result.getLogisticsCompany());
        assertEquals("SF1234567890", result.getTrackingNumber());
        assertEquals("PENDING", result.getStatus());
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    @DisplayName("创建物流 - 订单不存在应抛出异常")
    void createShipmentOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> shipmentService.createShipment(999L, "顺丰速运", "SF1234567890"));
    }

    @Test
    @DisplayName("根据ID获取物流")
    void getShipmentById() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));

        Shipment result = shipmentService.getShipmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("根据订单ID获取物流")
    void getShipmentByOrderId() {
        when(shipmentRepository.findByOrderId(1L)).thenReturn(Optional.of(testShipment));

        Shipment result = shipmentService.getShipmentByOrderId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getOrder().getId());
    }

    @Test
    @DisplayName("更新运单号")
    void updateTrackingNumber() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

        Shipment result = shipmentService.updateTrackingNumber(1L, "SF9999999999");

        assertNotNull(result);
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    @DisplayName("更新物流状态为已签收")
    void updateShipmentStatusDelivered() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

        Shipment result = shipmentService.updateShipmentStatus(1L, "DELIVERED");

        assertNotNull(result);
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    @DisplayName("根据状态获取物流列表")
    void getShipmentsByStatus() {
        when(shipmentRepository.findByStatus("PENDING")).thenReturn(List.of(testShipment));

        List<Shipment> result = shipmentService.getShipmentsByStatus("PENDING");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
