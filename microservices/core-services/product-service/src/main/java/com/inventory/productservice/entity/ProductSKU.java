package com.inventory.productservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 产品SKU实体类.
 *
 * <p>表示产品的具体规格型号，每个产品可以有多个SKU。
 * SKU（Stock Keeping Unit）是库存管理的基本单位。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Entity
@Table(name = "product_skus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class ProductSKU {

    /** SKU唯一标识符. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的产品. */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** SKU编码. */
    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;

    /** SKU属性. */
    @Column(name = "attributes", nullable = false)
    private String attributes;

    /** SKU价格. */
    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    /** 是否激活. */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** 创建时间. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建前回调.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新前回调.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
