package com.inventory.productservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 商品审核DTO.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class ProductAuditDTO {

    private Long id;
    private Long productId;
    private String oldStatus;
    private String newStatus;
    private Long auditorId;
    private String auditNotes;
    private LocalDateTime createdAt;
}
