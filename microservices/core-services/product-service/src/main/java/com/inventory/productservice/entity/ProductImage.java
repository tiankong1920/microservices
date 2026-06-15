package com.inventory.productservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 商品图片实体类.
 *
 * <p>表示商城系统中的商品图片信息。</p>
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class ProductImage {

    /** 图片ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商品. */
    @ManyToOne
    private Product product;

    /** 图片地址. */
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    /** 排序. */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 创建时间. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 创建前回调方法.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
