package com.inventory.adminservice.repository;

import com.inventory.adminservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPermissionName(String permissionName);
    Optional<Permission> findByPermissionCode(String permissionCode);
    boolean existsByPermissionName(String permissionName);
    boolean existsByPermissionCode(String permissionCode);
}
