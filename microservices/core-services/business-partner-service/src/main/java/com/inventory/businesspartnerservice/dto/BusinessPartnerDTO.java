package com.inventory.businesspartnerservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.inventory.businesspartnerservice.entity.BusinessPartner.PartnerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class BusinessPartnerDTO {

    private Long id;

    @NotBlank(message = "Partner code is required")
    @Size(max = 50, message = "Partner code must not exceed 50 characters")
    private String partnerCode;

    @NotBlank(message = "Partner name is required")
    @Size(max = 200, message = "Partner name must not exceed 200 characters")
    private String partnerName;

    private PartnerType partnerType;

    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Zip code must not exceed 20 characters")
    private String zipCode;

    private boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
