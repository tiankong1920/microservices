package com.inventory.orderservice.controller;

import com.inventory.orderservice.dto.OrderDTO;
import com.inventory.orderservice.dto.OrderItemDTO;
import com.inventory.orderservice.service.IOrderService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OrderControllerTest {

    @Mock
    private IOrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(1L);
        testOrderDTO.setOrderNumber("ORD-001");
        testOrderDTO.setCustomerName("Test Customer");
        testOrderDTO.setStatus("PENDING");
    }

    @Test
    void testGetAllOrders() {
        when(orderService.getAllOrders()).thenReturn(List.of(testOrderDTO));

        ResponseEntity<List<OrderDTO>> response = orderController.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ORD-001", response.getBody().get(0).getOrderNumber());
    }

    @Test
    void testGetOrderById() {
        when(orderService.getOrderById(1L)).thenReturn(testOrderDTO);

        ResponseEntity<OrderDTO> response = orderController.getOrderById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ORD-001", response.getBody().getOrderNumber());
    }

    @Test
    void testCreateOrder() {
        OrderDTO inputDTO = new OrderDTO();
        inputDTO.setOrderNumber("ORD-002");
        inputDTO.setCustomerName("New Customer");

        OrderDTO resultDTO = new OrderDTO();
        resultDTO.setId(2L);
        resultDTO.setOrderNumber("ORD-002");
        resultDTO.setCustomerName("New Customer");
        resultDTO.setStatus("PENDING");

        when(orderService.createOrder(any(OrderDTO.class))).thenReturn(resultDTO);

        ResponseEntity<OrderDTO> response = orderController.createOrder(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDING", response.getBody().getStatus());
    }

    @Test
    void testUpdateOrder() {
        OrderDTO inputDTO = new OrderDTO();
        inputDTO.setStatus("COMPLETED");

        OrderDTO resultDTO = new OrderDTO();
        resultDTO.setId(1L);
        resultDTO.setOrderNumber("ORD-001");
        resultDTO.setStatus("COMPLETED");

        when(orderService.updateOrder(eq(1L), any(OrderDTO.class))).thenReturn(resultDTO);

        ResponseEntity<OrderDTO> response = orderController.updateOrder(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("COMPLETED", response.getBody().getStatus());
    }

    @Test
    void testDeleteOrder() {
        ResponseEntity<Void> response = orderController.deleteOrder(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(orderService).deleteOrder(1L);
    }

    @Test
    void testGetOrderByOrderNumber() {
        when(orderService.findOrderByOrderNumber("ORD-001")).thenReturn(testOrderDTO);

        ResponseEntity<OrderDTO> response = orderController.getOrderByOrderNumber("ORD-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ORD-001", response.getBody().getOrderNumber());
    }

    @Test
    void testFindOrdersByCustomer() {
        when(orderService.findOrdersByCustomerName("Test Customer")).thenReturn(List.of(testOrderDTO));

        ResponseEntity<List<OrderDTO>> response = orderController.findOrdersByCustomer("Test Customer");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testFindOrdersByStatus() {
        when(orderService.findOrdersByStatus("PENDING")).thenReturn(List.of(testOrderDTO));

        ResponseEntity<List<OrderDTO>> response = orderController.findOrdersByStatus("PENDING");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetValidTransitions() {
        when(orderService.getValidStatusTransitions(1L)).thenReturn(List.of("CONFIRMED", "SHIPPED", "DELIVERED"));

        ResponseEntity<List<String>> response = orderController.getValidTransitions(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
    }

    @Test
    void testUpdateOrderStatus() {
        OrderDTO resultDTO = new OrderDTO();
        resultDTO.setId(1L);
        resultDTO.setStatus("CONFIRMED");

        when(orderService.updateOrderStatus(1L, "CONFIRMED")).thenReturn(resultDTO);

        ResponseEntity<OrderDTO> response = orderController.updateOrderStatus(1L, "CONFIRMED");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CONFIRMED", response.getBody().getStatus());
    }

    @Test
    void testCancelOrder() {
        OrderDTO cancelledDTO = new OrderDTO();
        cancelledDTO.setId(1L);
        cancelledDTO.setStatus("CANCELLED");

        when(orderService.cancelOrder(1L)).thenReturn(cancelledDTO);

        ResponseEntity<OrderDTO> response = orderController.cancelOrder(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CANCELLED", response.getBody().getStatus());
    }

    @Test
    void testAddItemToOrder() {
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(100L);
        itemDTO.setQuantity(2);

        OrderDTO resultDTO = new OrderDTO();
        resultDTO.setId(1L);
        resultDTO.setOrderNumber("ORD-001");

        when(orderService.addItemToOrder(eq(1L), any(OrderItemDTO.class))).thenReturn(resultDTO);

        ResponseEntity<OrderDTO> response = orderController.addItemToOrder(1L, itemDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testRemoveItemFromOrder() {
        OrderDTO resultDTO = new OrderDTO();
        resultDTO.setId(1L);
        resultDTO.setOrderNumber("ORD-001");

        when(orderService.removeItemFromOrder(1L, 100L)).thenReturn(resultDTO);

        ResponseEntity<OrderDTO> response = orderController.removeItemFromOrder(1L, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
