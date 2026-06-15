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
import com.inventory.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final InventoryClient inventoryClient;
    private final ModelMapper modelMapper;

    private static final Map<Order.Status, Set<Order.Status>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(Order.Status.PENDING,
                EnumSet.of(Order.Status.PROCESSING, Order.Status.CANCELLED));
        VALID_TRANSITIONS.put(Order.Status.PROCESSING,
                EnumSet.of(Order.Status.COMPLETED, Order.Status.CANCELLED));
        VALID_TRANSITIONS.put(Order.Status.COMPLETED, EnumSet.noneOf(Order.Status.class));
        VALID_TRANSITIONS.put(Order.Status.CANCELLED, EnumSet.noneOf(Order.Status.class));
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        log.info("Getting all orders");
        List<Order> orders = orderRepository.findAll();
        log.info("Found {} orders", orders.size());
        return orders.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        log.info("Getting order by id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.info("Creating order for customer: {}", orderDTO.getCustomerName());

        validateOrder(orderDTO);

        Order order = toEntity(orderDTO);
        order.setStatus(Order.Status.PENDING);

        if (order.getItems() != null) {
            List<OrderItem> successfullyDeducted = new ArrayList<>();

            try {
                for (OrderItem item : order.getItems()) {
                    item.setOrder(order);
                    boolean deducted = inventoryClient.deductStock(item.getProductId(), item.getQuantity());
                    if (!deducted) {
                        log.error("Failed to deduct stock for product id: {}", item.getProductId());
                        throw new InsufficientInventoryException(item.getProductId());
                    }
                    successfullyDeducted.add(item);
                }
            } catch (InsufficientInventoryException e) {
                log.warn("Inventory deduction failed, compensating previously deducted items");
                compensateInventoryDeduction(successfullyDeducted);
                throw e;
            }

            order.calculateTotalAmount();
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with id: {}", savedOrder.getId());
        return toDTO(savedOrder);
    }

    private void compensateInventoryDeduction(List<OrderItem> items) {
        for (OrderItem item : items) {
            try {
                boolean restored = inventoryClient.restoreStock(item.getProductId(), item.getQuantity());
                if (restored) {
                    log.info("Compensated: restored {} units for product id: {}",
                            item.getQuantity(), item.getProductId());
                } else {
                    log.error("Failed to compensate inventory for product id: {}", item.getProductId());
                }
            } catch (Exception ex) {
                log.error("Error compensating inventory for product id: {}", item.getProductId(), ex);
            }
        }
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(Long id, OrderDTO orderDTO) {
        log.info("Updating order with id: {}", id);

        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (orderDTO.getCustomerName() != null) {
            existingOrder.setCustomerName(orderDTO.getCustomerName());
        }

        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully with id: {}", updatedOrder.getId());
        return toDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() != Order.Status.PENDING && order.getStatus() != Order.Status.CANCELLED) {
            throw new OrderStatusException("Can only delete orders in PENDING or CANCELLED status, current status: " + order.getStatus());
        }

        List<OrderItem> successfullyRestored = new ArrayList<>();

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                try {
                    boolean restored = inventoryClient.restoreStock(item.getProductId(), item.getQuantity());
                    if (restored) {
                        successfullyRestored.add(item);
                    } else {
                        log.error("Failed to restore stock for product id: {}", item.getProductId());
                    }
                } catch (Exception e) {
                    log.error("Error restoring stock for product id: {}", item.getProductId(), e);
                }
            }
        }

        orderRepository.delete(order);
        log.info("Order deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long id, String newStatus) {
        log.info("Updating order {} status to: {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        Order.Status currentStatus = order.getStatus();
        Order.Status targetStatus = Order.Status.valueOf(newStatus.toUpperCase());

        validateStatusTransition(currentStatus, targetStatus);

        order.setStatus(targetStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated from {} to {}", id, currentStatus, targetStatus);
        return toDTO(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long id) {
        log.info("Cancelling order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        validateCancellableStatus(order.getStatus());
        restoreInventoryForCancellation(order);
        order.setStatus(Order.Status.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} cancelled successfully", id);
        return toDTO(updatedOrder);
    }

    private void validateCancellableStatus(Order.Status status) {
        if (status == Order.Status.COMPLETED) {
            throw new OrderStatusException("Cannot cancel a completed order");
        }
        if (status == Order.Status.CANCELLED) {
            throw new OrderStatusException("Order is already cancelled");
        }
        if (status != Order.Status.PENDING && status != Order.Status.PROCESSING) {
            throw new OrderStatusException("Cannot cancel order in status: " + status);
        }
    }

    private void restoreInventoryForCancellation(Order order) {
        List<OrderItem> successfullyRestored = new ArrayList<>();
        if (order.getItems() == null) {
            return;
        }
        for (OrderItem item : order.getItems()) {
            restoreItemStock(item, successfullyRestored);
        }
    }

    private void restoreItemStock(OrderItem item, List<OrderItem> successfullyRestored) {
        try {
            boolean restored = inventoryClient.restoreStock(item.getProductId(), item.getQuantity());
            if (restored) {
                successfullyRestored.add(item);
            } else {
                log.error("Failed to restore stock for product id: {}", item.getProductId());
                throw new OrderStatusException("Failed to restore inventory for product id: " + item.getProductId());
            }
        } catch (OrderStatusException e) {
            compensateCancelledOrderRestore(successfullyRestored);
            throw e;
        } catch (Exception e) {
            log.error("Error restoring stock for product id: {}", item.getProductId(), e);
            compensateCancelledOrderRestore(successfullyRestored);
            throw new OrderStatusException("Error restoring inventory for product id: " + item.getProductId());
        }
    }

    private void compensateCancelledOrderRestore(List<OrderItem> items) {
        for (OrderItem item : items) {
            try {
                inventoryClient.deductStock(item.getProductId(), item.getQuantity());
                log.info("Compensated: re-deducted {} units for product id: {}", item.getQuantity(), item.getProductId());
            } catch (Exception e) {
                log.error("Failed to compensate inventory restoration for product id: {}", item.getProductId(), e);
            }
        }
    }

    @Override
    public List<String> getValidStatusTransitions(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        Set<Order.Status> validNextStatuses = VALID_TRANSITIONS.getOrDefault(
                order.getStatus(), EnumSet.noneOf(Order.Status.class));

        return validNextStatuses.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> findOrdersByCustomerName(String customerName) {
        log.info("Finding orders by customer name: {}", customerName);
        return orderRepository.findByCustomerNameContainingIgnoreCase(customerName)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> findOrdersByStatus(String status) {
        log.info("Finding orders by status: {}", status);
        Order.Status orderStatus = Order.Status.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO findOrderByOrderNumber(String orderNumber) {
        log.info("Finding order by order number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
        return toDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO addItemToOrder(Long orderId, OrderItemDTO itemDTO) {
        log.info("Adding item to order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != Order.Status.PENDING) {
            throw new OrderStatusException("Can only add items to orders in PENDING status");
        }

        OrderItem item = modelMapper.map(itemDTO, OrderItem.class);
        item.setOrder(order);

        boolean deducted = inventoryClient.deductStock(item.getProductId(), item.getQuantity());
        if (!deducted) {
            throw new OrderStatusException("Insufficient inventory for product id: " + item.getProductId());
        }

        order.getItems().add(item);
        order.calculateTotalAmount();

        Order updatedOrder = orderRepository.save(order);
        log.info("Item added to order: {}", orderId);
        return toDTO(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDTO removeItemFromOrder(Long orderId, Long itemId) {
        log.info("Removing item {} from order: {}", itemId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != Order.Status.PENDING) {
            throw new OrderStatusException("Can only remove items from orders in PENDING status");
        }

        OrderItem itemToRemove = order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new OrderStatusException("Item not found in order: " + itemId));

        boolean restored = inventoryClient.restoreStock(itemToRemove.getProductId(), itemToRemove.getQuantity());
        if (!restored) {
            log.error("Failed to restore stock for product id: {}, item removal aborted", itemToRemove.getProductId());
            throw new OrderStatusException("Failed to restore inventory for product id: " + itemToRemove.getProductId());
        }

        order.getItems().remove(itemToRemove);
        order.calculateTotalAmount();

        orderItemRepository.delete(itemToRemove);

        Order updatedOrder = orderRepository.save(order);
        log.info("Item {} removed from order: {}", itemId, orderId);
        return toDTO(updatedOrder);
    }

    private void validateOrder(OrderDTO orderDTO) {
        if (orderDTO.getOrderNumber() == null || orderDTO.getOrderNumber().isBlank()) {
            throw new IllegalArgumentException("Order number is required");
        }
        if (orderDTO.getCustomerName() == null || orderDTO.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
    }

    private void validateStatusTransition(Order.Status current, Order.Status target) {
        Set<Order.Status> validTargets = VALID_TRANSITIONS.get(current);
        if (validTargets == null || !validTargets.contains(target)) {
            throw new OrderStatusException(
                    String.format("Invalid status transition from %s to %s", current, target));
        }
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = modelMapper.map(order, OrderDTO.class);
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        return dto;
    }

    private Order toEntity(OrderDTO dto) {
        Order order = modelMapper.map(dto, Order.class);
        if (dto.getStatus() != null) {
            order.setStatus(Order.Status.valueOf(dto.getStatus().toUpperCase()));
        }
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setCreatedAt(LocalDateTime.now()));
        }
        return order;
    }
}
