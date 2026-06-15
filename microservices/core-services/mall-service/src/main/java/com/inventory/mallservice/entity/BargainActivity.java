package com.inventory.mallservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 砍价活动实体类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Entity
@Table(name = "bargain_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class BargainActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "original_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalPrice;

    @Column(name = "min_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal minPrice;

    @Column(name = "max_bargain_count", nullable = false)
    private Integer maxBargainCount;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
