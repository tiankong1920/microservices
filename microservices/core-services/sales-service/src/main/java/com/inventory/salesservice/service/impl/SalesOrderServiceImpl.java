package com.inventory.salesservice.service.impl;

import com.inventory.salesservice.client.CustomerClient;
import com.inventory.salesservice.client.ProductClient;
import com.inventory.salesservice.dto.SalesOrderDTO;
import com.inventory.salesservice.dto.SalesOrderItemDTO;
import com.inventory.salesservice.entity.SalesOrder;
import com.inventory.salesservice.entity.SalesOrderItem;
import com.inventory.salesservice.exception.CustomerNotFoundException;
import com.inventory.salesservice.exception.ProductNotFoundException;
import com.inventory.salesservice.exception.SalesOrderNotFoundException;
import com.inventory.salesservice.repository.ISalesOrderItemRepository;
import com.inventory.salesservice.repository.ISalesOrderRepository;
import com.inventory.salesservice.service.ISalesOrderService;
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
public class SalesOrderServiceImpl implements ISalesOrderService {

    private final ISalesOrderRepository salesOrderRepository;
    private final ISalesOrderItemRepository salesOrderItemRepository;
    private final ModelMapper modelMapper;
    private final CustomerClient customerClient;
    private final ProductClient productClient;

    private static final String ERROR_SALES_ORDER_NOT_FOUND = "Sales order not found with id: ";
    private static final String ERROR_SALES_ORDER_NOT_FOUND_LOG = "Sales order not found with id: {}";
    private static final String ERROR_SALES_ORDER_NOT_FOUND_NUMBER = "Sales order not found with number: ";
    private static final String ERROR_SALES_ORDER_NOT_FOUND_NUMBER_LOG = "Sales order not found with number: {}";

    @Override
    public List<SalesOrderDTO> getAllSalesOrders() {
        log.info("Getting all sales orders");
        final List<SalesOrder> orders = salesOrderRepository.findAllWithItems();
        log.info("Found {} sales orders", orders.size());
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public SalesOrderDTO getSalesOrderById(Long id) {
        log.info("Getting sales order by id: {}", id);
        final SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_SALES_ORDER_NOT_FOUND_LOG, id);
                    return new SalesOrderNotFoundException(ERROR_SALES_ORDER_NOT_FOUND + id);
                });
        return mapToDTO(order);
    }

    @Override
    @Transactional
    public SalesOrderDTO createSalesOrder(SalesOrderDTO salesOrderDTO) {
        log.info("Creating sales order: {}", salesOrderDTO.getOrderNumber());

        validateCustomer(salesOrderDTO);
        validateProducts(salesOrderDTO);

        final SalesOrder order = createSalesOrderEntity(salesOrderDTO);
        final SalesOrder savedOrder = salesOrderRepository.save(order);

        if (salesOrderDTO.getItems() != null && !salesOrderDTO.getItems().isEmpty()) {
            processOrderItems(savedOrder, salesOrderDTO.getItems());
        }

        log.info("Sales order created successfully with id: {}", savedOrder.getId());
        return mapToDTO(savedOrder);
    }

    /**
 * Validate that the customer exists.
     *
 * @param salesOrderDTO the sales order DTO
 * @throws CustomerNotFoundException if customer is not found
     */
    private void validateCustomer(SalesOrderDTO salesOrderDTO) {
        final CustomerClient.CustomerDTO customer = customerClient.getCustomerById(salesOrderDTO.getCustomerId());
        if (customer == null) {
            log.error("Customer not found: {}", salesOrderDTO.getCustomerId());
            throw new CustomerNotFoundException(salesOrderDTO.getCustomerId());
        }
    }

    /**
 * Validate that all products in the order items exist.
     *
 * @param salesOrderDTO the sales order DTO
 * @throws ProductNotFoundException if any product is not found
     */
    private void validateProducts(SalesOrderDTO salesOrderDTO) {
        if (salesOrderDTO.getItems() != null && !salesOrderDTO.getItems().isEmpty()) {
            for (SalesOrderItemDTO itemDTO : salesOrderDTO.getItems()) {
                final ProductClient.ProductDTO product = productClient.getProductById(itemDTO.getProductId());
                if (product == null) {
                    log.error("Product not found: {}", itemDTO.getProductId());
                    throw new ProductNotFoundException(itemDTO.getProductId());
                }
            }
        }
    }

    /**
 * Create a SalesOrder entity from the DTO.
     *
 * @param salesOrderDTO the sales order DTO
 * @return the SalesOrder entity
     */
    private SalesOrder createSalesOrderEntity(SalesOrderDTO salesOrderDTO) {
        final SalesOrder order = modelMapper.map(salesOrderDTO, SalesOrder.class);
        order.setOrderDate(LocalDateTime.now());
        if (order.getStatus() == null) {
            order.setStatus("PENDING");
        }
        return order;
    }

    /**
 * Process and save sales order items, calculating and updating the subtotal.
     *
 * @param salesOrder the sales order entity
 * @param items the list of sales order items
     */
    private void processOrderItems(SalesOrder salesOrder, List<SalesOrderItemDTO> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalesOrderItemDTO itemDTO : items) {
            final SalesOrderItem item = modelMapper.map(itemDTO, SalesOrderItem.class);
            item.setSalesOrderId(salesOrder.getId());
            if (item.getSubtotal() == null) {
                final BigDecimal price = item.getUnitPrice().subtract(
                        item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO);
                item.setSubtotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            subtotal = subtotal.add(item.getSubtotal());
            salesOrderItemRepository.save(item);
        }
        salesOrder.setSubtotal(subtotal);
        salesOrder.setTotalAmount(subtotal);
        salesOrderRepository.save(salesOrder);
    }

    @Override
    @Transactional
    public SalesOrderDTO updateSalesOrder(Long id, SalesOrderDTO salesOrderDTO) {
        log.info("Updating sales order with id: {}", id);

        final SalesOrder existingOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_SALES_ORDER_NOT_FOUND_LOG, id);
                    return new SalesOrderNotFoundException(ERROR_SALES_ORDER_NOT_FOUND + id);
                });

        modelMapper.map(salesOrderDTO, existingOrder);
        final SalesOrder updatedOrder = salesOrderRepository.save(existingOrder);

        log.info("Sales order updated successfully with id: {}", updatedOrder.getId());
        return mapToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteSalesOrder(Long id) {
        log.info("Deleting sales order with id: {}", id);

        if (!salesOrderRepository.existsById(id)) {
            log.error(ERROR_SALES_ORDER_NOT_FOUND_LOG, id);
            throw new SalesOrderNotFoundException(ERROR_SALES_ORDER_NOT_FOUND + id);
        }

        salesOrderItemRepository.deleteBySalesOrderId(id);
        salesOrderRepository.deleteById(id);
        log.info("Sales order deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public SalesOrderDTO updateSalesOrderStatus(Long id, String status) {
        log.info("Updating sales order status to: {} for id: {}", status, id);

        final SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ERROR_SALES_ORDER_NOT_FOUND_LOG, id);
                    return new SalesOrderNotFoundException(ERROR_SALES_ORDER_NOT_FOUND + id);
                });

        order.setStatus(status);
        final SalesOrder updatedOrder = salesOrderRepository.save(order);

        log.info("Sales order status updated successfully with id: {}", updatedOrder.getId());
        return mapToDTO(updatedOrder);
    }

    @Override
    public Double getTotalSalesByCustomerId(Long customerId) {
        log.info("Getting total sales for customer with id: {}", customerId);
        return 0.0;
    }

    @Override
    public List<SalesOrderDTO> getSalesOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting sales orders between dates: {} and {}", startDate, endDate);
        final List<SalesOrder> orders = salesOrderRepository.findByOrderDateBetween(startDate, endDate);
        log.info("Found {} sales orders between dates", orders.size());
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<SalesOrderDTO> getSalesOrdersByStatus(String status) {
        log.info("Getting sales orders with status: {}", status);
        final List<SalesOrder> orders = salesOrderRepository.findByStatus(status);
        log.info("Found {} sales orders with status: {}", orders.size(), status);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<SalesOrderDTO> getSalesOrdersByCustomerId(Long customerId) {
        log.info("Getting sales orders for customer: {}", customerId);
        final List<SalesOrder> orders = salesOrderRepository.findByCustomerId(customerId);
        log.info("Found {} sales orders for customer: {}", orders.size(), customerId);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public SalesOrderDTO getSalesOrderByOrderNumber(String orderNumber) {
        log.info("Getting sales order by number: {}", orderNumber);
        final SalesOrder order = salesOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> {
                    log.error(ERROR_SALES_ORDER_NOT_FOUND_NUMBER_LOG, orderNumber);
                    return new SalesOrderNotFoundException(ERROR_SALES_ORDER_NOT_FOUND_NUMBER + orderNumber);
                });
        return mapToDTO(order);
    }

    private SalesOrderDTO mapToDTO(SalesOrder order) {
        final SalesOrderDTO dto = modelMapper.map(order, SalesOrderDTO.class);
        if (dto == null) {
            return new SalesOrderDTO();
        }
        final List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(order.getId());
        dto.setItems(items.stream()
                .map(item -> modelMapper.map(item, SalesOrderItemDTO.class))
                .toList());
        return dto;
    }
}
