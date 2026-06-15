package com.inventory.productservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
 * 产品实体类.
 *
 * <p>表示商城系统中的产品信息，包含基础信息、规格参数、图文详情等。</p>
 *
 * @author Inventory Team
 * @version 6.0
 * @since 3.0.0
 * @see ProductSKU
 * @see ProductCategory
 * @see ProductSpec
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class Product {

    /** 产品ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 产品编码. */
    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    /** 产品SKU代码. */
    @Column(name = "sku", unique = true)
    private String sku;

    /** 产品名称. */
    @Column(name = "name", nullable = false)
    private String name;

    /** 品牌. */
    @Column(name = "brand")
    private String brand;

    /** 商品分类. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    /** 售价. */
    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    /** 成本价. */
    @Column(name = "cost_price", precision = 19, scale = 4)
    private BigDecimal costPrice;

    /** 库存数量. */
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    /** 产品状态: DRAFT, PENDING, PUBLISHED, OFFLINE. */
    @Column(name = "status", nullable = false)
    private String status;

    /** 产品描述. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 主图. */
    @Column(name = "main_image")
    private String mainImage;

    /** 视频地址. */
    @Column(name = "video_url")
    private String videoUrl;

    /** 重量. */
    @Column(name = "weight", precision = 8, scale = 2)
    private BigDecimal weight;

    /** 尺寸. */
    @Column(name = "dimensions")
    private String dimensions;

    /** 创建人ID. */
    @Column(name = "created_by")
    private Long createdBy;

    /** 是否激活. */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** 创建时间. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 产品SKU列表. */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSKU> skus;

    /** 产品BOM列表. */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BOM> boms;

    /** 产品规格列表. */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSpec> specs;

    /** 产品图片列表. */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    /** 审核记录列表. */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAudit> audits;

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
