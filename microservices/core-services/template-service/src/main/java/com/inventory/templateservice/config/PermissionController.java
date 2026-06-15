package com.inventory.templateservice.config;

import com.inventory.templateservice.security.PermissionService;
import com.inventory.templateservice.security.TemplatePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * 权限管理控制器 - 管理模板权限
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "权限管理", description = "模板权限管理接口")
@Validated
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/template/{templateId}")
    @Operation(summary = "获取模板权限", description = "获取用户对指定模板的权限")
    public ResponseEntity<TemplatePermission> getTemplatePermission(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "角色列表") @RequestParam Set<String> roles) {
        TemplatePermission permission = permissionService.getPermission(templateId, userId, roles);
        return ResponseEntity.ok(permission);
    }

    @GetMapping("/check")
    @Operation(summary = "检查权限", description = "检查用户是否拥有指定权限")
    public ResponseEntity<Map<String, Object>> checkPermission(
            @Parameter(description = "模板ID") @RequestParam Long templateId,
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "角色列表") @RequestParam Set<String> roles,
            @Parameter(description = "权限") @RequestParam String permission) {
        boolean hasPermission = permissionService.hasPermission(templateId, userId, roles, permission);
        return ResponseEntity.ok(Map.of(
                "hasPermission", hasPermission,
                "templateId", templateId,
                "permission", permission
        ));
    }

    @GetMapping("/roles")
    @Operation(summary = "获取标准角色", description = "获取系统预定义的标准角色列表")
    public ResponseEntity<Set<String>> getStandardRoles() {
        return ResponseEntity.ok(permissionService.getStandardRoles());
    }

    @GetMapping("/roles/permissions")
    @Operation(summary = "获取角色权限映射", description = "获取所有角色及其对应的权限")
    public ResponseEntity<Map<String, Set<String>>> getAllRolePermissions() {
        return ResponseEntity.ok(permissionService.getAllRolePermissions());
    }

    @GetMapping("/roles/{role}/permissions")
    @Operation(summary = "获取角色权限", description = "获取指定角色的权限列表")
    public ResponseEntity<Set<String>> getRolePermissions(
            @Parameter(description = "角色名称") @PathVariable String role) {
        Map<String, Set<String>> allPermissions = permissionService.getAllRolePermissions();
        Set<String> permissions = allPermissions.getOrDefault(role, Set.of());
        return ResponseEntity.ok(permissions);
    }

    @PostMapping("/roles/permissions")
    @Operation(summary = "获取多角色权限", description = "获取多个角色合并后的权限列表")
    public ResponseEntity<Set<String>> getPermissionsForRoles(
            @RequestBody Set<String> roles) {
        Set<String> permissions = permissionService.getPermissionsForRoles(roles);
        return ResponseEntity.ok(permissions);
    }
}
