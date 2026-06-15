package com.inventory.orderservice.service;

import com.inventory.orderservice.dto.OrderDTO;
import com.inventory.orderservice.dto.OrderItemDTO;

import java.util.List;

public interface IOrderService {

    List<OrderDTO> getAllOrders();

    OrderDTO getOrderById(Long id);

    OrderDTO createOrder(OrderDTO orderDTO);

    OrderDTO updateOrder(Long id, OrderDTO orderDTO);

    void deleteOrder(Long id);

    OrderDTO updateOrderStatus(Long id, String newStatus);

    OrderDTO cancelOrder(Long id);

    List<String> getValidStatusTransitions(Long id);

    List<OrderDTO> findOrdersByCustomerName(String customerName);

    List<OrderDTO> findOrdersByStatus(String status);

    OrderDTO findOrderByOrderNumber(String orderNumber);

    OrderDTO addItemToOrder(Long orderId, OrderItemDTO itemDTO);

    OrderDTO removeItemFromOrder(Long orderId, Long itemId);
}
