package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.Shipment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 物流Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrderId(Long orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatus(String status);
}
