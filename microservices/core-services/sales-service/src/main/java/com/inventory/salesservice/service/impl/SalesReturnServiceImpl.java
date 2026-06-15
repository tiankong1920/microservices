package com.inventory.salesservice.service.impl;

import com.inventory.salesservice.dto.SalesReturnOrderDTO;
import com.inventory.salesservice.dto.SalesReturnItemDTO;
import com.inventory.salesservice.entity.SalesReturnOrder;
import com.inventory.salesservice.entity.SalesReturnItem;
import com.inventory.salesservice.repository.ISalesReturnOrderRepository;
import com.inventory.salesservice.repository.ISalesReturnItemRepository;
import com.inventory.salesservice.service.ISalesReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SalesReturnServiceImpl implements ISalesReturnService {

    private final ISalesReturnOrderRepository salesReturnOrderRepository;
    private final ISalesReturnItemRepository salesReturnItemRepository;
    private final ModelMapper modelMapper;

    private static final String ERROR_RETURN_ORDER_NOT_FOUND = "Sales return order not found with id: ";
    private static final String ERROR_RETURN_ORDER_NOT_FOUND_NUMBER = "Sales return order not found with number: ";
    private static final String LOG_RETURN_ORDER_NOT_FOUND_ID = ERROR_RETURN_ORDER_NOT_FOUND;

    @Override
    public List<SalesReturnOrderDTO> getAllSalesReturnOrders() {
        log.info("Getting all sales return orders");
        final List<SalesReturnOrder> orders = salesReturnOrderRepository.findAll();
        log.info("Found {} sales return orders", orders.size());
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public SalesReturnOrderDTO getSalesReturnOrderById(Long id) {
        log.info("Getting sales return order by id: {}", id);
        final SalesReturnOrder order = salesReturnOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_RETURN_ORDER_NOT_FOUND_ID, id);
                    return new RuntimeException(ERROR_RETURN_ORDER_NOT_FOUND + id);
                });
        return mapToDTO(order);
    }

    @Override
    public SalesReturnOrderDTO getSalesReturnOrderByReturnNumber(String returnNumber) {
        log.info("Getting sales return order by return number: {}", returnNumber);
        final SalesReturnOrder order = salesReturnOrderRepository.findByReturnNumber(returnNumber)
                .orElseThrow(() -> {
                    log.error("Sales return order not found with return number: {}", returnNumber);
                    return new RuntimeException(ERROR_RETURN_ORDER_NOT_FOUND_NUMBER + returnNumber);
                });
        return mapToDTO(order);
    }

    @Override
    public List<SalesReturnOrderDTO> getSalesReturnOrdersByCustomerId(Long customerId) {
        log.info("Getting sales return orders by customer id: {}", customerId);
        final List<SalesReturnOrder> orders = salesReturnOrderRepository.findByCustomerId(customerId);
        log.info("Found {} sales return orders for customer id: {}", orders.size(), customerId);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<SalesReturnOrderDTO> getSalesReturnOrdersByWarehouseId(Long warehouseId) {
        log.info("Getting sales return orders by warehouse id: {}", warehouseId);
        final List<SalesReturnOrder> orders = salesReturnOrderRepository.findByWarehouseId(warehouseId);
        log.info("Found {} sales return orders for warehouse id: {}", orders.size(), warehouseId);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<SalesReturnOrderDTO> getSalesReturnOrdersByStatus(String status) {
        log.info("Getting sales return orders by status: {}", status);
        final List<SalesReturnOrder> orders = salesReturnOrderRepository.findByStatus(status);
        log.info("Found {} sales return orders with status: {}", orders.size(), status);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<SalesReturnOrderDTO> getSalesReturnOrdersByOriginalOrderId(Long originalOrderId) {
        log.info("Getting sales return orders by original order id: {}", originalOrderId);
        final List<SalesReturnOrder> orders = salesReturnOrderRepository.findByOriginalOrderId(originalOrderId);
        log.info("Found {} sales return orders for original order id: {}", orders.size(), originalOrderId);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public SalesReturnOrderDTO createSalesReturnOrder(SalesReturnOrderDTO salesReturnOrderDTO) {
        log.info("Creating sales return order: {}", salesReturnOrderDTO.getReturnNumber());

        final SalesReturnOrder order = modelMapper.map(salesReturnOrderDTO, SalesReturnOrder.class);
        order.setReturnDate(LocalDateTime.now());
        if (order.getStatus() == null) {
            order.setStatus("PENDING");
        }

        final SalesReturnOrder savedOrder = salesReturnOrderRepository.save(order);

        if (salesReturnOrderDTO.getItems() != null && !salesReturnOrderDTO.getItems().isEmpty()) {
            BigDecimal subtotal = BigDecimal.ZERO;
            for (SalesReturnItemDTO itemDTO : salesReturnOrderDTO.getItems()) {
                final SalesReturnItem item = modelMapper.map(itemDTO, SalesReturnItem.class);
                item.setReturnOrderId(savedOrder.getId());
                if (item.getSubtotal() == null) {
                    final BigDecimal price = item.getUnitPrice().subtract(
                            item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO);
                    item.setSubtotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));
                }

                subtotal = subtotal.add(item.getSubtotal());
                salesReturnItemRepository.save(item);
            }
            savedOrder.setSubtotal(subtotal);
            savedOrder.setTotalAmount(subtotal);
            salesReturnOrderRepository.save(savedOrder);
        }

        log.info("Sales return order created successfully with id: {}", savedOrder.getId());
        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional
    public SalesReturnOrderDTO updateSalesReturnOrder(Long id, SalesReturnOrderDTO salesReturnOrderDTO) {
        log.info("Updating sales return order with id: {}", id);

        final SalesReturnOrder existingOrder = salesReturnOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_RETURN_ORDER_NOT_FOUND_ID, id);
                    return new RuntimeException(ERROR_RETURN_ORDER_NOT_FOUND + id);
                });

        modelMapper.map(salesReturnOrderDTO, existingOrder);
        final SalesReturnOrder updatedOrder = salesReturnOrderRepository.save(existingOrder);

        log.info("Sales return order updated successfully with id: {}", updatedOrder.getId());
        return mapToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteSalesReturnOrder(Long id) {
        log.info("Deleting sales return order with id: {}", id);

        if (!salesReturnOrderRepository.existsById(id)) {
            log.error(LOG_RETURN_ORDER_NOT_FOUND_ID, id);
            throw new IllegalStateException(ERROR_RETURN_ORDER_NOT_FOUND + id);
        }

        salesReturnItemRepository.deleteByReturnOrderId(id);
        salesReturnOrderRepository.deleteById(id);
        log.info("Sales return order deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public SalesReturnOrderDTO updateSalesReturnOrderStatus(Long id, String status) {
        log.info("Updating sales return order status to: {} for id: {}", status, id);

        final SalesReturnOrder order = salesReturnOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_RETURN_ORDER_NOT_FOUND_ID, id);
                    return new RuntimeException(ERROR_RETURN_ORDER_NOT_FOUND + id);
                });

        order.setStatus(status);
        final SalesReturnOrder updatedOrder = salesReturnOrderRepository.save(order);

        log.info("Sales return order status updated successfully with id: {}", updatedOrder.getId());
        return mapToDTO(updatedOrder);
    }

    @Override
    public Double getTotalReturnByCustomerId(Long customerId) {
        log.info("Getting total return by customer id: {}", customerId);
        final List<SalesReturnOrder> orders = salesReturnOrderRepository.findByCustomerId(customerId);
        final Double total = orders.stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        log.info("Total return for customer id {} is: {}", customerId, total);
        return total;
    }

    private SalesReturnOrderDTO mapToDTO(SalesReturnOrder order) {
        final SalesReturnOrderDTO dto = modelMapper.map(order, SalesReturnOrderDTO.class);
        final List<SalesReturnItem> items = salesReturnItemRepository.findByReturnOrderId(order.getId());
        dto.setItems(items.stream()
                .map(item -> modelMapper.map(item, SalesReturnItemDTO.class))
                .toList());
        return dto;
    }
}
