package com.inventory.inventoryservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDTO {

    private Long id;

    @NotBlank(message = "仓库编码不能为空")
    @Size(max = 50, message = "仓库编码长度不能超过50个字符")
    private String warehouseCode;

    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 200, message = "仓库名称长度不能超过200个字符")
    private String warehouseName;

    @Size(max = 500, message = "仓库地址长度不能超过500个字符")
    private String address;

    @Size(max = 100, message = "城市长度不能超过100个字符")
    private String city;

    @Size(max = 100, message = "省份长度不能超过100个字符")
    private String province;

    @Size(max = 100, message = "国家长度不能超过100个字符")
    private String country;

    @Pattern(regexp = "^[0-9A-Za-z-]{0,20}$", message = "邮政编码格式不正确")
    private String postalCode;

    @Size(max = 100, message = "联系人长度不能超过100个字符")
    private String contactPerson;

    @Pattern(regexp = "^[0-9+*-]{0,20}$", message = "联系电话格式不正确")
    private String contactPhone;

    @Email(message = "联系邮箱格式不正确")
    @Size(max = 200, message = "联系邮箱长度不能超过200个字符")
    private String contactEmail;

    @Min(value = 0, message = "仓库容量不能为负数")
    private Integer capacity;

    @Min(value = 0, message = "当前使用量不能为负数")
    private Integer currentUsage;

    private Boolean isActive;

    private Boolean isPrimary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
