package com.inventory.productservice.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 商品分类实体类.
 *
 * <p>表示商城系统中的商品分类信息，支持层级结构。</p>
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Entity
@Table(name = "product_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class ProductCategory {

    /** 分类ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分类名称. */
    @Column(name = "name", nullable = false)
    private String name;

    /** 父分类. */
    @ManyToOne
    private ProductCategory parent;

    /** 子分类. */
    @OneToMany(mappedBy = "parent")
    private List<ProductCategory> children;

    /** 层级. */
    @Column(name = "level", nullable = false)
    private Integer level;

    /** 排序. */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 状态. */
    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE

    /** 创建时间. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建前回调方法.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新前回调方法.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
