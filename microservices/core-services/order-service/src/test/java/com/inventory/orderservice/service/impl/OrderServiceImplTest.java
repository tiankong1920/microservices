package com.inventory.orderservice.service.impl;

import com.inventory.orderservice.client.InventoryClient;
import com.inventory.orderservice.dto.OrderDTO;
import com.inventory.orderservice.dto.OrderItemDTO;
import com.inventory.orderservice.entity.Order;
import com.inventory.orderservice.entity.OrderItem;
import com.inventory.orderservice.exception.InsufficientInventoryException;
import com.inventory.orderservice.exception.OrderNotFoundException;
import com.inventory.orderservice.exception.OrderStatusException;
import com.inventory.orderservice.repository.IOrderItemRepository;
import com.inventory.orderservice.repository.IOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OrderServiceImplTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IOrderItemRepository orderItemRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private List<OrderItem> testOrderItems;
    private List<OrderItemDTO> testOrderItemDTOs;

    @BeforeEach
    void setUp() {
        testOrderItems = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setProductName("Test Product");
        item.setQuantity(10);
        item.setUnitPrice(100.0);
        item.setCreatedAt(LocalDateTime.now());
        testOrderItems.add(item);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNumber("ORD-2024-0001");
        testOrder.setCustomerName("Test Customer");
        testOrder.setStatus(Order.Status.PENDING);
        testOrder.setItems(testOrderItems);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
        testOrderItems.forEach(i -> i.setOrder(testOrder));

        testOrderItemDTOs = new ArrayList<>();
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(100L);
        itemDTO.setProductName("Test Product");
        itemDTO.setQuantity(10);
        itemDTO.setUnitPrice(100.0);
        testOrderItemDTOs.add(itemDTO);

        testOrderDTO = new OrderDTO();
        testOrderDTO.setOrderNumber("ORD-2024-0001");
        testOrderDTO.setCustomerName("Test Customer");
        testOrderDTO.setItems(testOrderItemDTOs);
    }

    @Nested
    @DisplayName("Basic CRUD Tests")
    class BasicCrudTests {

        @Test
        @DisplayName("Should return all orders")
        void testGetAllOrders() {
            when(orderRepository.findAll()).thenReturn(List.of(testOrder));

            List<OrderDTO> result = orderService.getAllOrders();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("ORD-2024-0001", result.get(0).getOrderNumber());
            verify(orderRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no orders")
        void testGetAllOrdersEmpty() {
            when(orderRepository.findAll()).thenReturn(List.of());

            List<OrderDTO> result = orderService.getAllOrders();

            assertNotNull(result);
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("Should return order by ID")
        void testGetOrderById() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            OrderDTO result = orderService.getOrderById(1L);

            assertNotNull(result);
            assertEquals("ORD-2024-0001", result.getOrderNumber());
            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void testGetOrderByIdNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(999L));
        }

        @Test
        @DisplayName("Should create order with PENDING status")
        void testCreateOrder() {
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(inventoryClient.deductStock(any(), any())).thenReturn(true);

            OrderDTO result = orderService.createOrder(testOrderDTO);

            assertNotNull(result);
            assertEquals("PENDING", result.getStatus());
            verify(orderRepository).save(any(Order.class));
            verify(inventoryClient).deductStock(eq(100L), eq(10));
        }

        @Test
        @DisplayName("Should throw exception when inventory deduction fails")
        void testCreateOrderInventoryDeductionFails() {
            when(inventoryClient.deductStock(any(), any())).thenReturn(false);

            assertThrows(InsufficientInventoryException.class, () -> orderService.createOrder(testOrderDTO));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should update order customer name")
        void testUpdateOrder() {
            OrderDTO updateDTO = new OrderDTO();
            updateDTO.setCustomerName("Updated Customer");

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OrderDTO result = orderService.updateOrder(1L, updateDTO);

            assertNotNull(result);
            assertEquals("Updated Customer", result.getCustomerName());
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent order")
        void testUpdateOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            OrderDTO updateDTO = new OrderDTO();
            updateDTO.setStatus("COMPLETED");

            assertThrows(OrderNotFoundException.class, () -> orderService.updateOrder(999L, updateDTO));
        }

        @Test
        @DisplayName("Should delete order")
        void testDeleteOrder() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(inventoryClient.restoreStock(any(), any())).thenReturn(true);

            orderService.deleteOrder(1L);

            verify(orderRepository).delete(testOrder);
            verify(inventoryClient).restoreStock(eq(100L), eq(10));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent order")
        void testDeleteOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(999L));
        }
    }

    @Nested
    @DisplayName("Order Status Transition Tests")
    class OrderStatusTransitionTests {

        @Test
        @DisplayName("Should transition from PENDING to PROCESSING")
        void testTransitionPendingToProcessing() {
            testOrder.setStatus(Order.Status.PENDING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OrderDTO result = orderService.updateOrderStatus(1L, "PROCESSING");

            assertEquals("PROCESSING", result.getStatus());
            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("Should transition from PROCESSING to COMPLETED")
        void testTransitionProcessingToCompleted() {
            testOrder.setStatus(Order.Status.PROCESSING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OrderDTO result = orderService.updateOrderStatus(1L, "COMPLETED");

            assertEquals("COMPLETED", result.getStatus());
        }

        @Test
        @DisplayName("Should transition from PENDING to CANCELLED and restore inventory")
        void testTransitionPendingToCancelled() {
            testOrder.setStatus(Order.Status.PENDING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(inventoryClient.restoreStock(any(), any())).thenReturn(true);

            OrderDTO result = orderService.cancelOrder(1L);

            assertEquals("CANCELLED", result.getStatus());
            verify(inventoryClient).restoreStock(eq(100L), eq(10));
        }

        @Test
        @DisplayName("Should not allow transition from COMPLETED to any other status")
        void testCannotTransitionFromCompleted() {
            testOrder.setStatus(Order.Status.COMPLETED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(OrderStatusException.class, () -> orderService.updateOrderStatus(1L, "CANCELLED"));
        }

        @Test
        @DisplayName("Should not allow transition from CANCELLED to any other status")
        void testCannotTransitionFromCancelled() {
            testOrder.setStatus(Order.Status.CANCELLED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(OrderStatusException.class, () -> orderService.updateOrderStatus(1L, "PENDING"));
        }

        @Test
        @DisplayName("Should get valid status transitions")
        void testGetValidStatusTransitions() {
            testOrder.setStatus(Order.Status.PENDING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            List<String> validTransitions = orderService.getValidStatusTransitions(1L);

            assertNotNull(validTransitions);
            assertTrue(validTransitions.contains("PROCESSING"));
            assertTrue(validTransitions.contains("CANCELLED"));
        }
    }

    @Nested
    @DisplayName("Order Search Tests")
    class OrderSearchTests {

        @Test
        @DisplayName("Should find orders by customer name")
        void testFindOrdersByCustomerName() {
            when(orderRepository.findByCustomerNameContainingIgnoreCase("Test"))
                    .thenReturn(List.of(testOrder));

            List<OrderDTO> result = orderService.findOrdersByCustomerName("Test");

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderRepository).findByCustomerNameContainingIgnoreCase("Test");
        }

        @Test
        @DisplayName("Should find orders by status")
        void testFindOrdersByStatus() {
            when(orderRepository.findByStatus(Order.Status.PENDING)).thenReturn(List.of(testOrder));

            List<OrderDTO> result = orderService.findOrdersByStatus("PENDING");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("PENDING", result.get(0).getStatus());
        }

        @Test
        @DisplayName("Should find orders by order number")
        void testFindOrderByOrderNumber() {
            when(orderRepository.findByOrderNumber("ORD-2024-0001"))
                    .thenReturn(Optional.of(testOrder));

            OrderDTO result = orderService.findOrderByOrderNumber("ORD-2024-0001");

            assertNotNull(result);
            assertEquals("ORD-2024-0001", result.getOrderNumber());
        }
    }

    @Nested
    @DisplayName("Order Amount Calculation Tests")
    class OrderAmountCalculationTests {

        @Test
        @DisplayName("Should calculate order total amount")
        void testCalculateOrderTotalAmount() {
            testOrderItemDTOs.clear();
            OrderItemDTO item1 = new OrderItemDTO();
            item1.setProductId(101L);
            item1.setQuantity(2);
            item1.setUnitPrice(100.0);
            testOrderItemDTOs.add(item1);

            OrderItemDTO item2 = new OrderItemDTO();
            item2.setProductId(102L);
            item2.setQuantity(3);
            item2.setUnitPrice(50.0);
            testOrderItemDTOs.add(item2);

            testOrderDTO.setItems(testOrderItemDTOs);

            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });
            when(inventoryClient.deductStock(any(), any())).thenReturn(true);

            OrderDTO result = orderService.createOrder(testOrderDTO);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertEquals(350.0, savedOrder.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("Order Item Management Tests")
    class OrderItemManagementTests {

        @Test
        @DisplayName("Should add item to existing order")
        void testAddItemToOrder() {
            OrderItemDTO newItem = new OrderItemDTO();
            newItem.setProductId(200L);
            newItem.setProductName("New Product");
            newItem.setQuantity(5);
            newItem.setUnitPrice(200.0);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(inventoryClient.deductStock(any(), any())).thenReturn(true);

            OrderDTO result = orderService.addItemToOrder(1L, newItem);

            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should remove item from order and restore inventory")
        void testRemoveItemFromOrder() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(inventoryClient.restoreStock(any(), any())).thenReturn(true);

            OrderDTO result = orderService.removeItemFromOrder(1L, 1L);

            assertNotNull(result);
            verify(inventoryClient).restoreStock(eq(100L), eq(10));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should reject order with empty order number")
        void testRejectOrderWithEmptyOrderNumber() {
            testOrderDTO.setOrderNumber("");

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(testOrderDTO));
        }

        @Test
        @DisplayName("Should reject order with null customer name")
        void testRejectOrderWithNullCustomerName() {
            testOrderDTO.setCustomerName(null);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(testOrderDTO));
        }

        @Test
        @DisplayName("Should reject order with empty items")
        void testRejectOrderWithEmptyItems() {
            testOrderDTO.setItems(new ArrayList<>());

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(testOrderDTO));
        }
    }

    @Nested
    @DisplayName("Delete Order Edge Cases")
    class DeleteOrderEdgeCases {

        @Test
        @DisplayName("Should throw exception when deleting order in PROCESSING status")
        void testDeleteOrderProcessingStatus() {
            testOrder.setStatus(Order.Status.PROCESSING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(OrderStatusException.class, () -> orderService.deleteOrder(1L));
        }

        @Test
        @DisplayName("Should throw exception when deleting order in COMPLETED status")
        void testDeleteOrderCompletedStatus() {
            testOrder.setStatus(Order.Status.COMPLETED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(OrderStatusException.class, () -> orderService.deleteOrder(1L));
        }
    }

    @Nested
    @DisplayName("Cancel Order Edge Cases")
    class CancelOrderEdgeCases {

        @Test
        @DisplayName("Should throw exception when cancelling already cancelled order")
        void testCancelAlreadyCancelledOrder() {
            testOrder.setStatus(Order.Status.CANCELLED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(OrderStatusException.class, () -> orderService.cancelOrder(1L));
        }

        @Test
        @DisplayName("Should throw exception when cancelling completed order")
        void testCancelCompletedOrder() {
            testOrder.setStatus(Order.Status.COMPLETED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(OrderStatusException.class, () -> orderService.cancelOrder(1L));
        }

        @Test
        @DisplayName("Should compensate when restore stock fails during cancellation")
        void testCancelOrderRestoreStockFails() {
            testOrder.setStatus(Order.Status.PENDING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(inventoryClient.restoreStock(any(), any())).thenReturn(false);

            assertThrows(OrderStatusException.class, () -> orderService.cancelOrder(1L));
        }
    }

    @Nested
    @DisplayName("Add Item Edge Cases")
    class AddItemEdgeCases {

        @Test
        @DisplayName("Should throw exception when adding item to non-PENDING order")
        void testAddItemToNonPendingOrder() {
            testOrder.setStatus(Order.Status.PROCESSING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            OrderItemDTO newItem = new OrderItemDTO();
            newItem.setProductId(200L);
            newItem.setQuantity(5);

            assertThrows(OrderStatusException.class, () -> orderService.addItemToOrder(1L, newItem));
        }

        @Test
        @DisplayName("Should throw exception when adding item with insufficient inventory")
        void testAddItemInsufficientInventory() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(inventoryClient.deductStock(any(), any())).thenReturn(false);

            OrderItemDTO newItem = new OrderItemDTO();
            newItem.setProductId(200L);
            newItem.setQuantity(5);

            assertThrows(OrderStatusException.class, () -> orderService.addItemToOrder(1L, newItem));
        }
    }

    @Nested
    @DisplayName("Remove Item Edge Cases")
    class RemoveItemEdgeCases {

        @Test
        @DisplayName("Should throw exception when removing item from non-PENDING order")
        void testRemoveItemFromNonPendingOrder() {
            testOrder.setStatus(Order.Status.PROCESSING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(OrderStatusException.class, () -> orderService.removeItemFromOrder(1L, 1L));
        }

        @Test
        @DisplayName("Should throw exception when item not found in order")
        void testRemoveItemNotFound() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(OrderStatusException.class, () -> orderService.removeItemFromOrder(1L, 999L));
        }
    }
}
