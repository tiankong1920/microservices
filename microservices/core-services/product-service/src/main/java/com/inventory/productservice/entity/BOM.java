package com.inventory.productservice.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
 * 物料清单实体类.
 *
 * <p>表示产品的物料清单（Bill of Materials），定义了产品的组成结构。
 * 每个产品可以有一个或多个BOM，用于生产管理和成本核算。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Entity
@Table(name = "boms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class BOM {

    /** BOM唯一标识符. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的产品. */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** BOM编码. */
    @Column(name = "bom_code", nullable = false, unique = true)
    private String bomCode;

    /** BOM描述. */
    @Column(name = "description")
    private String description;

    /** 是否激活. */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** 创建时间. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** BOM组件列表. */
    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BOMComponent> components;

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
