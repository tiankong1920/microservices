package com.inventory.inventoryservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAlertDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private String alertType;
    private Integer currentQuantity;
    private Integer thresholdValue;
    private String alertEmail;
    private boolean enabled;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
