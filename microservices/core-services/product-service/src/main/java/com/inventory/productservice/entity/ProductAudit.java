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
 * 商品审核记录实体类.
 *
 * <p>表示商城系统中商品上下架审核的记录信息。</p>
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Entity
@Table(name = "product_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class ProductAudit {

    /** 审核ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 商品. */
    @ManyToOne
    private Product product;

    /** 旧状态. */
    @Column(name = "old_status", nullable = false)
    private String oldStatus;

    /** 新状态. */
    @Column(name = "new_status", nullable = false)
    private String newStatus;

    /** 审核人ID. */
    @Column(name = "auditor_id")
    private Long auditorId;

    /** 审核备注. */
    @Column(name = "audit_notes")
    private String auditNotes;

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
