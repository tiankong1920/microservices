package com.inventory.salesservice.service.impl;

import com.inventory.salesservice.dto.RetailOrderDTO;
import com.inventory.salesservice.dto.RetailOrderItemDTO;
import com.inventory.salesservice.entity.RetailOrder;
import com.inventory.salesservice.entity.RetailOrderItem;
import com.inventory.salesservice.repository.IRetailOrderRepository;
import com.inventory.salesservice.repository.IRetailOrderItemRepository;
import com.inventory.salesservice.service.IRetailService;
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
public class RetailServiceImpl implements IRetailService {

    private final IRetailOrderRepository retailOrderRepository;
    private final IRetailOrderItemRepository retailOrderItemRepository;
    private final ModelMapper modelMapper;

    private static final String ERROR_RETAIL_ORDER_NOT_FOUND = "Retail order not found with id: ";
    private static final String ERROR_RETAIL_ORDER_NOT_FOUND_NUMBER = "Retail order not found with number: ";
    private static final String LOG_RETAIL_ORDER_NOT_FOUND_ID = ERROR_RETAIL_ORDER_NOT_FOUND;

    @Override
    public List<RetailOrderDTO> getAllRetailOrders() {
        log.info("Getting all retail orders");
        final List<RetailOrder> orders = retailOrderRepository.findAll();
        log.info("Found {} retail orders", orders.size());
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public RetailOrderDTO getRetailOrderById(Long id) {
        log.info("Getting retail order by id: {}", id);
        final RetailOrder order = retailOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_RETAIL_ORDER_NOT_FOUND_ID, id);
                    return new RuntimeException(ERROR_RETAIL_ORDER_NOT_FOUND + id);
                });
        return mapToDTO(order);
    }

    @Override
    public RetailOrderDTO getRetailOrderByRetailNumber(String retailNumber) {
        log.info("Getting retail order by retail number: {}", retailNumber);
        final RetailOrder order = retailOrderRepository.findByRetailNumber(retailNumber)
                .orElseThrow(() -> {
                    log.error("Retail order not found with retail number: {}", retailNumber);
                    return new RuntimeException(ERROR_RETAIL_ORDER_NOT_FOUND_NUMBER + retailNumber);
                });
        return mapToDTO(order);
    }

    @Override
    public List<RetailOrderDTO> getRetailOrdersByCustomerId(Long customerId) {
        log.info("Getting retail orders by customer id: {}", customerId);
        final List<RetailOrder> orders = retailOrderRepository.findByCustomerId(customerId);
        log.info("Found {} retail orders for customer id: {}", orders.size(), customerId);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<RetailOrderDTO> getRetailOrdersByWarehouseId(Long warehouseId) {
        log.info("Getting retail orders by warehouse id: {}", warehouseId);
        final List<RetailOrder> orders = retailOrderRepository.findByWarehouseId(warehouseId);
        log.info("Found {} retail orders for warehouse id: {}", orders.size(), warehouseId);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<RetailOrderDTO> getRetailOrdersByPaymentStatus(String paymentStatus) {
        log.info("Getting retail orders by payment status: {}", paymentStatus);
        final List<RetailOrder> orders = retailOrderRepository.findByPaymentStatus(paymentStatus);
        log.info("Found {} retail orders with payment status: {}", orders.size(), paymentStatus);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<RetailOrderDTO> getRetailOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting retail orders by date range: {} to {}", startDate, endDate);
        final List<RetailOrder> orders = retailOrderRepository.findByRetailDateBetween(startDate, endDate);
        log.info("Found {} retail orders in date range", orders.size());
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public RetailOrderDTO createRetailOrder(RetailOrderDTO retailOrderDTO) {
        log.info("Creating retail order: {}", retailOrderDTO.getRetailNumber());

        final RetailOrder order = modelMapper.map(retailOrderDTO, RetailOrder.class);
        order.setRetailDate(LocalDateTime.now());
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus("UNPAID");
        }

        final RetailOrder savedOrder = retailOrderRepository.save(order);

        if (retailOrderDTO.getItems() != null && !retailOrderDTO.getItems().isEmpty()) {
            BigDecimal subtotal = BigDecimal.ZERO;
            for (RetailOrderItemDTO itemDTO : retailOrderDTO.getItems()) {
                final RetailOrderItem item = modelMapper.map(itemDTO, RetailOrderItem.class);
                item.setRetailOrderId(savedOrder.getId());
                if (item.getSubtotal() == null) {
                    final BigDecimal price = item.getUnitPrice().subtract(
                            item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO);
                    item.setSubtotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));
                }

                subtotal = subtotal.add(item.getSubtotal());
                retailOrderItemRepository.save(item);
            }
            savedOrder.setSubtotal(subtotal);
            savedOrder.setTotalAmount(subtotal);
            retailOrderRepository.save(savedOrder);
        }

        log.info("Retail order created successfully with id: {}", savedOrder.getId());
        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional
    public RetailOrderDTO updateRetailOrder(Long id, RetailOrderDTO retailOrderDTO) {
        log.info("Updating retail order with id: {}", id);

        final RetailOrder existingOrder = retailOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_RETAIL_ORDER_NOT_FOUND_ID, id);
                    return new RuntimeException(ERROR_RETAIL_ORDER_NOT_FOUND + id);
                });

        modelMapper.map(retailOrderDTO, existingOrder);
        final RetailOrder updatedOrder = retailOrderRepository.save(existingOrder);

        log.info("Retail order updated successfully with id: {}", updatedOrder.getId());
        return mapToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteRetailOrder(Long id) {
        log.info("Deleting retail order with id: {}", id);

        if (!retailOrderRepository.existsById(id)) {
            log.error(LOG_RETAIL_ORDER_NOT_FOUND_ID, id);
            throw new IllegalStateException(ERROR_RETAIL_ORDER_NOT_FOUND + id);
        }

        retailOrderItemRepository.deleteByRetailOrderId(id);
        retailOrderRepository.deleteById(id);
        log.info("Retail order deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public RetailOrderDTO updateRetailOrderPaymentStatus(Long id, String paymentStatus) {
        log.info("Updating retail order payment status to: {} for id: {}", paymentStatus, id);

        final RetailOrder order = retailOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(LOG_RETAIL_ORDER_NOT_FOUND_ID, id);
                    return new RuntimeException(ERROR_RETAIL_ORDER_NOT_FOUND + id);
                });

        order.setPaymentStatus(paymentStatus);
        final RetailOrder updatedOrder = retailOrderRepository.save(order);

        log.info("Retail order payment status updated successfully with id: {}", updatedOrder.getId());
        return mapToDTO(updatedOrder);
    }

    @Override
    public Double getTotalRetailByCustomerId(Long customerId) {
        log.info("Getting total retail by customer id: {}", customerId);
        final List<RetailOrder> orders = retailOrderRepository.findByCustomerId(customerId);
        final Double total = orders.stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        log.info("Total retail for customer id {} is: {}", customerId, total);
        return total;
    }

    private RetailOrderDTO mapToDTO(RetailOrder order) {
        final RetailOrderDTO dto = modelMapper.map(order, RetailOrderDTO.class);
        final List<RetailOrderItem> items = retailOrderItemRepository.findByRetailOrderId(order.getId());
        dto.setItems(items.stream()
                .map(item -> modelMapper.map(item, RetailOrderItemDTO.class))
                .toList());
        return dto;
    }
}
