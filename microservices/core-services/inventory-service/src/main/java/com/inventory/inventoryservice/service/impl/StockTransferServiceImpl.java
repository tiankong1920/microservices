package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.StockTransferOrderDTO;
import com.inventory.inventoryservice.dto.StockTransferItemDTO;
import com.inventory.inventoryservice.entity.StockTransferOrder;
import com.inventory.inventoryservice.entity.StockTransferItem;
import com.inventory.inventoryservice.repository.IStockTransferOrderRepository;
import com.inventory.inventoryservice.repository.IStockTransferItemRepository;
import com.inventory.inventoryservice.service.IStockTransferService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class StockTransferServiceImpl implements IStockTransferService {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(StockTransferServiceImpl.class);

    private final IStockTransferOrderRepository stockTransferOrderRepository;
    private final IStockTransferItemRepository stockTransferItemRepository;
    private final ModelMapper modelMapper;

    private static final String ERROR_TRANSFER_ORDER_NOT_FOUND = "Stock transfer order not found with id: ";
    private static final String ERROR_TRANSFER_ORDER_NOT_FOUND_NUMBER = "Stock transfer order not found with number: ";

    @Override
    public List<StockTransferOrderDTO> getAllStockTransferOrders() {
        LOG.info("Getting all stock transfer orders");
        final List<StockTransferOrder> orders = stockTransferOrderRepository.findAll();
        LOG.info("Found {} stock transfer orders", orders.size());
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public StockTransferOrderDTO getStockTransferOrderById(Long id) {
        LOG.info("Getting stock transfer order by id: {}", id);
        final StockTransferOrder order = stockTransferOrderRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Stock transfer order not found with id: {}", id);
                    return new RuntimeException(ERROR_TRANSFER_ORDER_NOT_FOUND + id);
                });
        return mapToDTO(order);
    }

    @Override
    public StockTransferOrderDTO getStockTransferOrderByTransferNumber(String transferNumber) {
        LOG.info("Getting stock transfer order by transfer number: {}", transferNumber);
        final StockTransferOrder order = stockTransferOrderRepository.findByTransferNumber(transferNumber)
                .orElseThrow(() -> {
                    LOG.error("Stock transfer order not found with transfer number: {}", transferNumber);
                    return new RuntimeException(ERROR_TRANSFER_ORDER_NOT_FOUND_NUMBER + transferNumber);
                });
        return mapToDTO(order);
    }

    @Override
    public List<StockTransferOrderDTO> getStockTransferOrdersBySourceWarehouse(Long sourceWarehouseId) {
        LOG.info("Getting stock transfer orders by source warehouse id: {}", sourceWarehouseId);
        final List<StockTransferOrder> orders = stockTransferOrderRepository.findBySourceWarehouseId(sourceWarehouseId);
        LOG.info("Found {} stock transfer orders for source warehouse id: {}", orders.size(), sourceWarehouseId);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<StockTransferOrderDTO> getStockTransferOrdersByTargetWarehouse(Long targetWarehouseId) {
        LOG.info("Getting stock transfer orders by target warehouse id: {}", targetWarehouseId);
        final List<StockTransferOrder> orders = stockTransferOrderRepository.findByTargetWarehouseId(targetWarehouseId);
        LOG.info("Found {} stock transfer orders for target warehouse id: {}", orders.size(), targetWarehouseId);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<StockTransferOrderDTO> getStockTransferOrdersByStatus(String status) {
        LOG.info("Getting stock transfer orders by status: {}", status);
        final List<StockTransferOrder> orders = stockTransferOrderRepository.findByStatus(status);
        LOG.info("Found {} stock transfer orders with status: {}", orders.size(), status);
        return orders.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public StockTransferOrderDTO createStockTransferOrder(StockTransferOrderDTO stockTransferOrderDTO) {
        LOG.info("Creating stock transfer order: {}", stockTransferOrderDTO.getTransferNumber());

        final StockTransferOrder order = modelMapper.map(stockTransferOrderDTO, StockTransferOrder.class);
        order.setTransferDate(LocalDateTime.now());
        if (order.getStatus() == null) {
            order.setStatus("PENDING");
        }

        final StockTransferOrder savedOrder = stockTransferOrderRepository.save(order);

        if (stockTransferOrderDTO.getItems() != null && !stockTransferOrderDTO.getItems().isEmpty()) {
            int totalQuantity = 0;
            for (StockTransferItemDTO itemDTO : stockTransferOrderDTO.getItems()) {
                final StockTransferItem item = modelMapper.map(itemDTO, StockTransferItem.class);
                item.setTransferOrderId(savedOrder.getId());
                totalQuantity += item.getQuantity();
                stockTransferItemRepository.save(item);
            }
            savedOrder.setTotalQuantity(totalQuantity);
            stockTransferOrderRepository.save(savedOrder);
        }

        LOG.info("Stock transfer order created successfully with id: {}", savedOrder.getId());
        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional
    public StockTransferOrderDTO updateStockTransferOrder(Long id, StockTransferOrderDTO stockTransferOrderDTO) {
        LOG.info("Updating stock transfer order with id: {}", id);

        final StockTransferOrder existingOrder = stockTransferOrderRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Stock transfer order not found with id: {}", id);
                    return new RuntimeException(ERROR_TRANSFER_ORDER_NOT_FOUND + id);
                });

        modelMapper.map(stockTransferOrderDTO, existingOrder);
        final StockTransferOrder updatedOrder = stockTransferOrderRepository.save(existingOrder);

        LOG.info("Stock transfer order updated successfully with id: {}", updatedOrder.getId());
        return mapToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteStockTransferOrder(Long id) {
        LOG.info("Deleting stock transfer order with id: {}", id);

        if (!stockTransferOrderRepository.existsById(id)) {
            LOG.error("Stock transfer order not found with id: {}", id);
            throw new IllegalStateException(ERROR_TRANSFER_ORDER_NOT_FOUND + id);
        }

        stockTransferItemRepository.deleteByTransferOrderId(id);
        stockTransferOrderRepository.deleteById(id);
        LOG.info("Stock transfer order deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public StockTransferOrderDTO updateStockTransferOrderStatus(Long id, String status) {
        LOG.info("Updating stock transfer order status to: {} for id: {}", status, id);

        final StockTransferOrder order = stockTransferOrderRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Stock transfer order not found with id: {}", id);
                    return new RuntimeException(ERROR_TRANSFER_ORDER_NOT_FOUND + id);
                });

        order.setStatus(status);
        if ("COMPLETED".equals(status)) {
            order.setActualTransferDate(LocalDateTime.now());
        }
        final StockTransferOrder updatedOrder = stockTransferOrderRepository.save(order);

        LOG.info("Stock transfer order status updated successfully with id: {}", updatedOrder.getId());
        return mapToDTO(updatedOrder);
    }

    @Override
    public Integer getTotalTransferByWarehouse(Long warehouseId) {
        LOG.info("Getting total transfer by warehouse id: {}", warehouseId);
        final List<StockTransferOrder> sourceOrders = stockTransferOrderRepository.findBySourceWarehouseId(warehouseId);
        final List<StockTransferOrder> targetOrders = stockTransferOrderRepository.findByTargetWarehouseId(warehouseId);
        final Integer total = sourceOrders.stream()
                .map(order -> order.getTotalQuantity() != null ? order.getTotalQuantity() : 0)
                .reduce(0, Integer::sum)
                - targetOrders.stream()
                .map(order -> order.getTotalQuantity() != null ? order.getTotalQuantity() : 0)
                .reduce(0, Integer::sum);
        LOG.info("Total transfer for warehouse id {} is: {}", warehouseId, total);
        return total;
    }

    private StockTransferOrderDTO mapToDTO(StockTransferOrder order) {
        final StockTransferOrderDTO dto = modelMapper.map(order, StockTransferOrderDTO.class);
        final List<StockTransferItem> items = stockTransferItemRepository.findByTransferOrderId(order.getId());
        dto.setItems(items.stream()
                .map(item -> modelMapper.map(item, StockTransferItemDTO.class))
                .toList());
        return dto;
    }
}
