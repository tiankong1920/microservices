package com.inventory.salesservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "retail_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetailOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "retail_number", nullable = false, unique = true, length = 50)
    private String retailNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "retail_date", nullable = false)
    private LocalDateTime retailDate;

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "subtotal", precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "tax", precision = 19, scale = 4)
    private BigDecimal tax;

    @Column(name = "discount", precision = 19, scale = 4)
    private BigDecimal discount;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 19, scale = 4)
    private BigDecimal paidAmount;

    @Column(name = "change_amount", precision = 19, scale = 4)
    private BigDecimal changeAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (retailDate == null) {
            retailDate = LocalDateTime.now();
        }
        if (paymentStatus == null) {
            paymentStatus = "UNPAID";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
