package com.invoice.invoiceservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoice_pinyin_index", indexes = {
    @Index(name = "idx_pinyin_initials", columnList = "pinyin_initials"),
    @Index(name = "idx_pinyin_entity", columnList = "entity_type,entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PinyinIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_name", nullable = false, length = 200)
    private String entityName;

    @Column(name = "pinyin_initials", nullable = false, length = 50)
    private String pinyinInitials;

    @Column(name = "pinyin_full", length = 500)
    private String pinyinFull;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;
}
