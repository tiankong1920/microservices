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
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 商品规格实体类.
 *
 * <p>表示商品的规格信息，如颜色、尺寸等。</p>
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Entity
@Table(name = "product_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class ProductSpec {

    /** 规格ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商品. */
    @ManyToOne
    private Product product;

    /** 规格名称. */
    @Column(name = "spec_name", nullable = false)
    private String specName;

    /** 规格值列表. */
    @OneToMany(mappedBy = "spec")
    private List<ProductSpecValue> values;

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
