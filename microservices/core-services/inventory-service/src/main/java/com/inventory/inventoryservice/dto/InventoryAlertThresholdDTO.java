package com.inventory.inventoryservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAlertThresholdDTO {

    private Long id;

    private Long productId;

    private Long warehouseId;

    @NotNull(message = "Low stock threshold is required")
    @Min(value = 0, message = "Low stock threshold must be non-negative")
    private Integer lowStockThreshold;

    @NotNull(message = "Critical stock threshold is required")
    @Min(value = 0, message = "Critical stock threshold must be non-negative")
    private Integer criticalStockThreshold;

    @NotNull(message = "Reorder point is required")
    @Min(value = 0, message = "Reorder point must be non-negative")
    private Integer reorderPoint;

    @Email(message = "Alert email must be valid")
    private String alertEmail;

    private boolean enabled;
}
