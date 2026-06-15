package com.inventory.adminservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_permission")
@Getter
@Setter
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_name", nullable = false, length = 50)
    private String permissionName;

    @Column(name = "permission_code", unique = true, nullable = false, length = 50)
    private String permissionCode;

    @Column(name = "resource_type", nullable = false, length = 20)
    private String resourceType;

    @Column(name = "resource_path", length = 255)
    private String resourcePath;

    @Column(name = "method", length = 10)
    private String method;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
