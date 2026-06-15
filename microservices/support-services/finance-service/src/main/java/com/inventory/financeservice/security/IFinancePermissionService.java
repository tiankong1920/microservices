package com.inventory.financeservice.security;

import java.util.List;
import java.util.Set;

public interface IFinancePermissionService {

    boolean hasPermission(String userId, String permission);

    boolean hasAnyPermission(String userId, String... permissions);

    boolean hasAllPermissions(String userId, String... permissions);

    boolean hasRole(String userId, String roleCode);

    boolean hasDataAccess(String userId, String dataScope, Long departmentId, Long projectId);

    Set<String> getUserPermissions(String userId);

    Set<String> getRolePermissions(String roleCode);

    List<FinanceUser> getUsersByRole(String roleCode);

    List<FinanceUser> getUsersByDataScope(String dataScope);

    void addPermissionToUser(String userId, String permission);

    void removePermissionFromUser(String userId, String permission);

    void assignRoleToUser(String userId, String roleCode);

    void revokeRoleFromUser(String userId, String roleCode);

    FinanceRole createRole(String roleCode, String roleName, String description, Set<String> permissions);

    void updateRolePermissions(String roleCode, Set<String> permissions);

    void deactivateRole(String roleCode);

    PermissionCheckResult checkPermission(String userId, String permission, Object resource);

    class PermissionCheckResult {
        private boolean allowed;
        private String reason;
        private String requiredPermission;

        public PermissionCheckResult(boolean allowed, String reason, String requiredPermission) {
            this.allowed = allowed;
            this.reason = reason;
            this.requiredPermission = requiredPermission;
        }

        public boolean isAllowed() { return allowed; }
        public String getReason() { return reason; }
        public String getRequiredPermission() { return requiredPermission; }
    }
}
