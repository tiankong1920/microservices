package com.inventory.financeservice.security;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "finance_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", length = 200)
    private String departmentName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private FinanceRole role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "finance_user_additional_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "additional_permission")
    @Builder.Default
    private Set<String> additionalPermissions = new HashSet<>();

    @Column(name = "data_scope", length = 50)
    @Builder.Default
    private String dataScope = "OWN";

    @Column(name = "mfa_enabled")
    @Builder.Default
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_secret", length = 200)
    private String mfaSecret;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Set<String> getAllPermissions() {
        Set<String> allPermissions = new HashSet<>();
        if (role != null && role.getPermissions() != null) {
            allPermissions.addAll(role.getPermissions());
        }
        if (additionalPermissions != null) {
            allPermissions.addAll(additionalPermissions);
        }
        return allPermissions;
    }

    public boolean hasPermission(String permission) {
        return getAllPermissions().contains(permission);
    }

    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }
}
