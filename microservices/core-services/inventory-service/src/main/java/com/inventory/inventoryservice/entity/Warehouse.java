package com.inventory.inventoryservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_code", nullable = false, unique = true, length = 50)
    private String warehouseCode;

    @Column(name = "warehouse_name", nullable = false, length = 100)
    private String warehouseName;

    @Column(name = "address")
    private String address;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "current_usage")
    private Integer currentUsage;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Getters
    public Long getId() { return id; }
    public String getWarehouseCode() { return warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getCountry() { return country; }
    public String getPostalCode() { return postalCode; }
    public String getContactPerson() { return contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public Integer getCapacity() { return capacity; }
    public Integer getCurrentUsage() { return currentUsage; }
    public Boolean getActive() { return active; }
    public Boolean getPrimary() { return primary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setProvince(String province) { this.province = province; }
    public void setCountry(String country) { this.country = country; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setCurrentUsage(Integer currentUsage) { this.currentUsage = currentUsage; }
    public void setActive(Boolean active) { this.active = active; }
    public void setPrimary(Boolean primary) { this.primary = primary; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (primary == null) {
            primary = false;
        }
        if (currentUsage == null) {
            currentUsage = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
