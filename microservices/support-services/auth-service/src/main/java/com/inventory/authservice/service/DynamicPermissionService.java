package com.inventory.authservice.service;

import com.inventory.authservice.entity.Permission;
import com.inventory.authservice.entity.Role;
import com.inventory.authservice.entity.User;
import com.inventory.authservice.repository.IPermissionRepository;
import com.inventory.authservice.repository.IRoleRepository;
import com.inventory.authservice.repository.IUserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class DynamicPermissionService {

    private static final String PERMISSION_CACHE = "permissions";
    private static final String USER_PERMISSIONS_CACHE = "userPermissions";

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;
    private final SecurityAuditService securityAuditService;

    public DynamicPermissionService(
            IUserRepository userRepository,
            IRoleRepository roleRepository,
            IPermissionRepository permissionRepository,
            SecurityAuditService securityAuditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.securityAuditService = securityAuditService;
    }

    @Cacheable(value = USER_PERMISSIONS_CACHE, key = "#username")
    @Transactional(readOnly = true)
    public Set<String> getUserPermissions(String username) {
        return userRepository.findByUsername(username)
            .map(user -> user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
    }

    @Cacheable(value = USER_PERMISSIONS_CACHE, key = "#username + '_roles'")
    @Transactional(readOnly = true)
    public Set<String> getUserRoles(String username) {
        return userRepository.findByUsername(username)
            .map(user -> user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(String username, String permission) {
        return getUserPermissions(username).contains(permission);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyPermission(String username, String... permissions) {
        Set<String> userPermissions = getUserPermissions(username);
        return Arrays.stream(permissions).anyMatch(userPermissions::contains);
    }

    @Transactional(readOnly = true)
    public boolean hasAllPermissions(String username, String... permissions) {
        Set<String> userPermissions = getUserPermissions(username);
        return Arrays.stream(permissions).allMatch(userPermissions::contains);
    }

    @Transactional(readOnly = true)
    public boolean hasRole(String username, String roleName) {
        return getUserRoles(username).contains(roleName);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyRole(String username, String... roleNames) {
        Set<String> userRoles = getUserRoles(username);
        return Arrays.stream(roleNames).anyMatch(userRoles::contains);
    }

    @CacheEvict(value = USER_PERMISSIONS_CACHE, key = "#username")
    public void assignRoleToUser(String username, String roleName, String assignedBy) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        Role role = roleRepository.findByRoleName(roleName)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        
        user.getRoles().add(role);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        securityAuditService.logPermissionChange(
            username, assignedBy, 
            "Role assigned: " + roleName + " to user " + username
        );
    }

    @CacheEvict(value = USER_PERMISSIONS_CACHE, key = "#username")
    public void removeRoleFromUser(String username, String roleName, String removedBy) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        Role role = roleRepository.findByRoleName(roleName)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        
        user.getRoles().remove(role);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        securityAuditService.logPermissionChange(
            username, removedBy,
            "Role removed: " + roleName + " from user " + username
        );
    }

    @CacheEvict(value = PERMISSION_CACHE, allEntries = true)
    public Permission createPermission(String name, String resource, String action, String description) {
        Permission permission = new Permission();
        permission.setPermissionName(name);
        permission.setResource(resource);
        permission.setAction(action);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }

    @CacheEvict(value = PERMISSION_CACHE, allEntries = true)
    public void assignPermissionToRole(String roleName, String permissionName, String assignedBy) {
        Role role = roleRepository.findByRoleName(roleName)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        
        Permission permission = permissionRepository.findByPermissionName(permissionName)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionName));
        
        role.getPermissions().add(permission);
        roleRepository.save(role);
        
        securityAuditService.logPermissionChange(
            "ROLE_" + roleName, assignedBy,
            "Permission assigned: " + permissionName + " to role " + roleName
        );
    }

    @CacheEvict(value = PERMISSION_CACHE, allEntries = true)
    public void removePermissionFromRole(String roleName, String permissionName, String removedBy) {
        Role role = roleRepository.findByRoleName(roleName)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        
        Permission permission = permissionRepository.findByPermissionName(permissionName)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionName));
        
        role.getPermissions().remove(permission);
        roleRepository.save(role);
        
        securityAuditService.logPermissionChange(
            "ROLE_" + roleName, removedBy,
            "Permission removed: " + permissionName + " from role " + roleName
        );
    }

    @CacheEvict(value = USER_PERMISSIONS_CACHE, key = "#username")
    public void refreshUserPermissions(String username) {
        // Cache eviction only - no additional logic needed
    }

    @CacheEvict(value = {PERMISSION_CACHE, USER_PERMISSIONS_CACHE}, allEntries = true)
    public void refreshAllPermissions() {
        // Cache eviction only - no additional logic needed
    }

    @Transactional(readOnly = true)
    public Page<Permission> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Role> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Role> getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }
}
