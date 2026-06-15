package com.inventory.supplierservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
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
public class SupplierDTO {

    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 200, message = "Supplier name must not exceed 200 characters")
    private String name;

    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^[0-9+]*$", message = "Phone must contain only digits")
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "Product category must not exceed 100 characters")
    private String productCategory;

    @Builder.Default
    private boolean active = true;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
