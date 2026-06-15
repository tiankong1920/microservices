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
@Table(name = "sales_return_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_order_id", nullable = false)
    private Long returnOrderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "product_sku", length = 100)
    private String productSku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "discount", precision = 19, scale = 4)
    private BigDecimal discount;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "subtotal", precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "tax", precision = 19, scale = 4)
    private BigDecimal tax;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
