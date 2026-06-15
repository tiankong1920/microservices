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
 * 优惠券模板实体类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Entity
@Table(name = "coupon_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class CouponTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type; // FIXED, PERCENTAGE, CATEGORY

    @Column(name = "value", nullable = false, precision = 19, scale = 4)
    private BigDecimal value;

    @Column(name = "min_spend", precision = 19, scale = 4)
    private BigDecimal minSpend;

    @Column(name = "max_discount", precision = 19, scale = 4)
    private BigDecimal maxDiscount;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE

    @Column(name = "applicable_categories", columnDefinition = "JSONB")
    private String applicableCategories;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
