package com.inventory.inventoryservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_alert_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAlertLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "alert_type", nullable = false, length = 20)
    private String alertType;

    @Column(name = "current_quantity", nullable = false)
    private Integer currentQuantity;

    @Column(name = "threshold_value", nullable = false)
    private Integer thresholdValue;

    @Column(name = "email_sent_to")
    private String emailSentTo;

    @Column(name = "email_status", length = 20)
    private String emailStatus;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by")
    private String acknowledgedBy;

    public enum AlertType {
        LOW_STOCK,
        CRITICAL_STOCK,
        OUT_OF_STOCK,
        REORDER_POINT
    }

    public enum EmailStatus {
        PENDING,
        SENT,
        FAILED
    }
}
