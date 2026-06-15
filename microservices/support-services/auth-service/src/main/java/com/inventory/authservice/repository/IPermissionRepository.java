package com.inventory.authservice.repository;

import com.inventory.authservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermissionName(String permissionName);

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);

    List<Permission> findByResourceAndAction(String resource, String action);

    boolean existsByPermissionName(String permissionName);
}
