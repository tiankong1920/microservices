package com.inventory.businesspartnerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_partners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String partnerCode;

    @Column(nullable = false)
    private String partnerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerType partnerType;

    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String zipCode;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PartnerType {
        CUSTOMER, SUPPLIER, BOTH
    }
}
