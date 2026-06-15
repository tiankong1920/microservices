package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.Shipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 物流服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IShipmentService {

    Shipment createShipment(Long orderId, String logisticsCompany, String trackingNumber);

    Shipment getShipmentById(Long id);

    Shipment getShipmentByOrderId(Long orderId);

    Shipment updateTrackingNumber(Long id, String trackingNumber);

    Shipment updateShipmentStatus(Long id, String status);

    Page<Shipment> getShipments(Pageable pageable);

    List<Shipment> getShipmentsByStatus(String status);

    void syncLogisticsInfo(Long shipmentId);
}
