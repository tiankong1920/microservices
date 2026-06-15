package com.inventory.productservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 物料清单组件实体类.
 *
 * <p>表示物料清单中的单个组件，定义了组成产品的具体物料。
 * 每个组件包含物料编码、名称、数量、计量单位等信息。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
@Entity
@Table(name = "bom_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class BOMComponent {

    /** 组件唯一标识符. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的BOM. */
    @ManyToOne
    @JoinColumn(name = "bom_id", nullable = false)
    private BOM bom;

    /** 组件编码. */
    @Column(name = "component_code", nullable = false)
    private String componentCode;

    /** 组件名称. */
    @Column(name = "component_name", nullable = false)
    private String componentName;

    /** 数量. */
    @Column(name = "quantity", nullable = false)
    private Double quantity;

    /** 计量单位. */
    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure;

    /** 组件描述. */
    @Column(name = "description")
    private String description;
}
