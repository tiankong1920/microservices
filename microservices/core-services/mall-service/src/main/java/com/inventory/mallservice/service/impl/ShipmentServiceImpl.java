package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.Order;
import com.inventory.mallservice.entity.Shipment;
import com.inventory.mallservice.repository.IOrderRepository;
import com.inventory.mallservice.repository.IShipmentRepository;
import com.inventory.mallservice.service.IShipmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class ShipmentServiceImpl implements IShipmentService {

    private final IShipmentRepository shipmentRepository;
    private final IOrderRepository orderRepository;

    @Override
    @Transactional
    public Shipment createShipment(final Long orderId, final String logisticsCompany,
                                    final String trackingNumber) {
        log.info("Creating shipment for order {} with tracking number {}", orderId, trackingNumber);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Order", orderId));
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setLogisticsCompany(logisticsCompany);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setStatus("PENDING");
        shipment.setEstimatedDelivery(LocalDateTime.now().plusDays(3));
        return shipmentRepository.save(shipment);
    }

    @Override
    public Shipment getShipmentById(final Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Shipment", id));
    }

    @Override
    public Shipment getShipmentByOrderId(final Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> EntityNotFoundException.forEntityWithField("Shipment", "orderId", orderId));
    }

    @Override
    @Transactional
    public Shipment updateTrackingNumber(final Long id, final String trackingNumber) {
        log.info("Updating tracking number for shipment {} to {}", id, trackingNumber);
        Shipment shipment = getShipmentById(id);
        shipment.setTrackingNumber(trackingNumber);
        return shipmentRepository.save(shipment);
    }

    @Override
    @Transactional
    public Shipment updateShipmentStatus(final Long id, final String status) {
        log.info("Updating shipment {} status to {}", id, status);
        Shipment shipment = getShipmentById(id);
        shipment.setStatus(status);
        if ("DELIVERED".equals(status)) {
            shipment.setActualDelivery(LocalDateTime.now());
        }
        return shipmentRepository.save(shipment);
    }

    @Override
    public Page<Shipment> getShipments(final Pageable pageable) {
        return shipmentRepository.findAll(pageable);
    }

    @Override
    public List<Shipment> getShipmentsByStatus(final String status) {
        return shipmentRepository.findByStatus(status);
    }

    @Override
    public void syncLogisticsInfo(final Long shipmentId) {
        log.info("Syncing logistics info for shipment {}", shipmentId);
        Shipment shipment = getShipmentById(shipmentId);
        log.info("Current shipment status: {}, tracking: {}",
                shipment.getStatus(), shipment.getTrackingNumber());
    }
}
