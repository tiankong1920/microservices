package com.inventory.authservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "oauth2_permissions")
@Getter
@Setter
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_name", unique = true, nullable = false, length = 100)
    private String permissionName;

    @Column(name = "resource", length = 100)
    private String resource;

    @Column(name = "action", length = 50)
    private String action;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToMany(mappedBy = "permissions", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
}
