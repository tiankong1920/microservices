package com.inventory.adminservice.repository;

import com.inventory.adminservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
    Optional<Role> findByRoleCode(String roleCode);
    boolean existsByRoleName(String roleName);
    boolean existsByRoleCode(String roleCode);
}
