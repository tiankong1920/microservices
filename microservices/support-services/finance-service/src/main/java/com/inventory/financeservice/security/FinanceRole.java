package com.inventory.financeservice.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "finance_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "role_level", nullable = false)
    private Integer roleLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "finance_role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission")
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    @Column(name = "is_system_role")
    @Builder.Default
    private Boolean isSystemRole = false;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String ROLE_FINANCE_DIRECTOR = "FINANCE_DIRECTOR";
    public static final String ROLE_FINANCE_MANAGER = "FINANCE_MANAGER";
    public static final String ROLE_CASHIER = "CASHIER";
    public static final String ROLE_ACCOUNTANT = "ACCOUNTANT";
    public static final String ROLE_DEPARTMENT_HEAD = "DEPARTMENT_HEAD";
    public static final String ROLE_QUERY_USER = "QUERY_USER";

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
