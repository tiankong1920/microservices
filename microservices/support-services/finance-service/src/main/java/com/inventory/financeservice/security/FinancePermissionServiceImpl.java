package com.inventory.financeservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinancePermissionServiceImpl implements IFinancePermissionService {

    private final IFinanceUserRepository userRepository;
    private final IFinanceRoleRepository roleRepository;

    public static final String PERMISSION_ACCOUNT_CREATE = "ACCOUNT_CREATE";
    public static final String PERMISSION_ACCOUNT_EDIT = "ACCOUNT_EDIT";
    public static final String PERMISSION_ACCOUNT_DELETE = "ACCOUNT_DELETE";
    public static final String PERMISSION_ACCOUNT_VIEW = "ACCOUNT_VIEW";

    public static final String PERMISSION_INCOME_CREATE = "INCOME_CREATE";
    public static final String PERMISSION_INCOME_EDIT = "INCOME_EDIT";
    public static final String PERMISSION_INCOME_DELETE = "INCOME_DELETE";
    public static final String PERMISSION_INCOME_APPROVE = "INCOME_APPROVE";
    public static final String PERMISSION_INCOME_VIEW = "INCOME_VIEW";

    public static final String PERMISSION_EXPENSE_CREATE = "EXPENSE_CREATE";
    public static final String PERMISSION_EXPENSE_EDIT = "EXPENSE_EDIT";
    public static final String PERMISSION_EXPENSE_DELETE = "EXPENSE_DELETE";
    public static final String PERMISSION_EXPENSE_APPROVE = "EXPENSE_APPROVE";
    public static final String PERMISSION_EXPENSE_VIEW = "EXPENSE_VIEW";

    public static final String PERMISSION_BUDGET_CREATE = "BUDGET_CREATE";
    public static final String PERMISSION_BUDGET_EDIT = "BUDGET_EDIT";
    public static final String PERMISSION_BUDGET_APPROVE = "BUDGET_APPROVE";
    public static final String PERMISSION_BUDGET_VIEW = "BUDGET_VIEW";

    public static final String PERMISSION_REPORT_GENERATE = "REPORT_GENERATE";
    public static final String PERMISSION_REPORT_VIEW = "REPORT_VIEW";
    public static final String PERMISSION_REPORT_EXPORT = "REPORT_EXPORT";

    public static final String PERMISSION_TAX_VIEW = "TAX_VIEW";
    public static final String PERMISSION_TAX_MANAGE = "TAX_MANAGE";

    public static final String PERMISSION_USER_MANAGE = "USER_MANAGE";
    public static final String PERMISSION_ROLE_MANAGE = "ROLE_MANAGE";
    public static final String PERMISSION_SYSTEM_CONFIG = "SYSTEM_CONFIG";

    public static final String PERMISSION_AUDIT_VIEW = "AUDIT_VIEW";

    public static final String DATA_SCOPE_OWN = "OWN";
    public static final String DATA_SCOPE_DEPARTMENT = "DEPARTMENT";
    public static final String DATA_SCOPE_ALL = "ALL";

    @Override
    public boolean hasPermission(String userId, String permission) {
        FinanceUser user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            log.warn("User not found: {}", userId);
            return false;
        }

        if (user.isLocked()) {
            log.warn("User is locked: {}", userId);
            return false;
        }

        return user.hasPermission(permission);
    }

    @Override
    public boolean hasAnyPermission(String userId, String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(userId, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(String userId, String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(userId, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasRole(String userId, String roleCode) {
        FinanceUser user = userRepository.findByUserId(userId).orElse(null);
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole().getRoleCode().equals(roleCode);
    }

    @Override
    public boolean hasDataAccess(String userId, String dataScope, Long departmentId, Long projectId) {
        FinanceUser user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            return false;
        }

        String userDataScope = user.getDataScope();

        if (DATA_SCOPE_ALL.equals(userDataScope)) {
            return true;
        }

        if (DATA_SCOPE_DEPARTMENT.equals(userDataScope)) {
            if (user.getDepartmentId() != null && user.getDepartmentId().equals(departmentId)) {
                return true;
            }
        }

        if (DATA_SCOPE_OWN.equals(userDataScope)) {
            return true;
        }

        return false;
    }

    @Override
    public Set<String> getUserPermissions(String userId) {
        FinanceUser user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            return Collections.emptySet();
        }
        return user.getAllPermissions();
    }

    @Override
    public Set<String> getRolePermissions(String roleCode) {
        FinanceRole role = roleRepository.findByRoleCode(roleCode).orElse(null);
        if (role == null) {
            return Collections.emptySet();
        }
        return role.getPermissions();
    }

    @Override
    public List<FinanceUser> getUsersByRole(String roleCode) {
        return userRepository.findByRoleCode(roleCode);
    }

    @Override
    public List<FinanceUser> getUsersByDataScope(String dataScope) {
        return userRepository.findByDataScope(dataScope);
    }

    @Override
    @Transactional
    public void addPermissionToUser(String userId, String permission) {
        FinanceUser user = userRepository.findByUserId(userId).orElse(null);
        if (user != null) {
            user.getAdditionalPermissions().add(permission);
            userRepository.save(user);
            log.info("Added permission {} to user {}", permission, userId);
        }
    }

    @Override
    @Transactional
    public void removePermissionFromUser(String userId, String permission) {
        FinanceUser user = userRepository.findByUserId(userId).orElse(null);
        if (user != null) {
            user.getAdditionalPermissions().remove(permission);
            userRepository.save(user);
            log.info("Removed permission {} from user {}", permission, userId);
        }
    }

    @Override
    @Transactional
    public void assignRoleToUser(String userId, String roleCode) {
        FinanceUser user = userRepository.findByUserId(userId).orElse(null);
        FinanceRole role = roleRepository.findByRoleCode(roleCode).orElse(null);

        if (user != null && role != null) {
            user.setRole(role);
            userRepository.save(user);
            log.info("Assigned role {} to user {}", roleCode, userId);
        }
    }

    @Override
    @Transactional
    public void revokeRoleFromUser(String userId, String roleCode) {
        FinanceUser user = userRepository.findByUserId(userId).orElse(null);
        if (user != null && user.getRole() != null && user.getRole().getRoleCode().equals(roleCode)) {
            user.setRole(null);
            userRepository.save(user);
            log.info("Revoked role {} from user {}", roleCode, userId);
        }
    }

    @Override
    @Transactional
    public FinanceRole createRole(String roleCode, String roleName, String description, Set<String> permissions) {
        FinanceRole role = FinanceRole.builder()
                .roleCode(roleCode)
                .roleName(roleName)
                .description(description)
                .permissions(permissions)
                .roleLevel(permissions.size())
                .isSystemRole(false)
                .status("ACTIVE")
                .build();

        FinanceRole saved = roleRepository.save(role);
        log.info("Created role: {}", roleCode);
        return saved;
    }

    @Override
    @Transactional
    public void updateRolePermissions(String roleCode, Set<String> permissions) {
        FinanceRole role = roleRepository.findByRoleCode(roleCode).orElse(null);
        if (role != null) {
            role.setPermissions(permissions);
            roleRepository.save(role);
            log.info("Updated permissions for role: {}", roleCode);
        }
    }

    @Override
    @Transactional
    public void deactivateRole(String roleCode) {
        FinanceRole role = roleRepository.findByRoleCode(roleCode).orElse(null);
        if (role != null) {
            role.setStatus("INACTIVE");
            roleRepository.save(role);
            log.info("Deactivated role: {}", roleCode);
        }
    }

    @Override
    public PermissionCheckResult checkPermission(String userId, String permission, Object resource) {
        if (!hasPermission(userId, permission)) {
            return new PermissionCheckResult(false, "User does not have required permission: " + permission, permission);
        }

        FinanceUser user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            return new PermissionCheckResult(false, "User not found", permission);
        }

        if (user.isLocked()) {
            return new PermissionCheckResult(false, "User account is locked", permission);
        }

        if (resource instanceof FinanceResource financeResource) {
            if (!hasDataAccess(userId, financeResource.getDataScope(), financeResource.getDepartmentId(), financeResource.getProjectId())) {
                return new PermissionCheckResult(false, "User does not have data access to this resource", permission);
            }
        }

        return new PermissionCheckResult(true, "Permission granted", permission);
    }

    public static Set<String> getSystemAdminPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add(PERMISSION_ACCOUNT_CREATE);
        permissions.add(PERMISSION_ACCOUNT_EDIT);
        permissions.add(PERMISSION_ACCOUNT_DELETE);
        permissions.add(PERMISSION_ACCOUNT_VIEW);
        permissions.add(PERMISSION_INCOME_CREATE);
        permissions.add(PERMISSION_INCOME_EDIT);
        permissions.add(PERMISSION_INCOME_DELETE);
        permissions.add(PERMISSION_INCOME_APPROVE);
        permissions.add(PERMISSION_INCOME_VIEW);
        permissions.add(PERMISSION_EXPENSE_CREATE);
        permissions.add(PERMISSION_EXPENSE_EDIT);
        permissions.add(PERMISSION_EXPENSE_DELETE);
        permissions.add(PERMISSION_EXPENSE_APPROVE);
        permissions.add(PERMISSION_EXPENSE_VIEW);
        permissions.add(PERMISSION_BUDGET_CREATE);
        permissions.add(PERMISSION_BUDGET_EDIT);
        permissions.add(PERMISSION_BUDGET_APPROVE);
        permissions.add(PERMISSION_BUDGET_VIEW);
        permissions.add(PERMISSION_REPORT_GENERATE);
        permissions.add(PERMISSION_REPORT_VIEW);
        permissions.add(PERMISSION_REPORT_EXPORT);
        permissions.add(PERMISSION_TAX_VIEW);
        permissions.add(PERMISSION_TAX_MANAGE);
        permissions.add(PERMISSION_USER_MANAGE);
        permissions.add(PERMISSION_ROLE_MANAGE);
        permissions.add(PERMISSION_SYSTEM_CONFIG);
        permissions.add(PERMISSION_AUDIT_VIEW);
        return permissions;
    }

    public static Set<String> getFinanceDirectorPermissions() {
        Set<String> permissions = getSystemAdminPermissions();
        return permissions;
    }

    public static Set<String> getFinanceManagerPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add(PERMISSION_ACCOUNT_VIEW);
        permissions.add(PERMISSION_INCOME_CREATE);
        permissions.add(PERMISSION_INCOME_EDIT);
        permissions.add(PERMISSION_INCOME_APPROVE);
        permissions.add(PERMISSION_INCOME_VIEW);
        permissions.add(PERMISSION_EXPENSE_CREATE);
        permissions.add(PERMISSION_EXPENSE_EDIT);
        permissions.add(PERMISSION_EXPENSE_APPROVE);
        permissions.add(PERMISSION_EXPENSE_VIEW);
        permissions.add(PERMISSION_BUDGET_CREATE);
        permissions.add(PERMISSION_BUDGET_EDIT);
        permissions.add(PERMISSION_BUDGET_APPROVE);
        permissions.add(PERMISSION_BUDGET_VIEW);
        permissions.add(PERMISSION_REPORT_GENERATE);
        permissions.add(PERMISSION_REPORT_VIEW);
        permissions.add(PERMISSION_REPORT_EXPORT);
        permissions.add(PERMISSION_TAX_VIEW);
        permissions.add(PERMISSION_AUDIT_VIEW);
        return permissions;
    }

    public static Set<String> getCashierPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add(PERMISSION_ACCOUNT_VIEW);
        permissions.add(PERMISSION_INCOME_CREATE);
        permissions.add(PERMISSION_INCOME_VIEW);
        permissions.add(PERMISSION_EXPENSE_CREATE);
        permissions.add(PERMISSION_EXPENSE_VIEW);
        permissions.add(PERMISSION_REPORT_VIEW);
        return permissions;
    }

    public static Set<String> getAccountantPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add(PERMISSION_ACCOUNT_VIEW);
        permissions.add(PERMISSION_ACCOUNT_EDIT);
        permissions.add(PERMISSION_INCOME_CREATE);
        permissions.add(PERMISSION_INCOME_EDIT);
        permissions.add(PERMISSION_INCOME_VIEW);
        permissions.add(PERMISSION_EXPENSE_CREATE);
        permissions.add(PERMISSION_EXPENSE_EDIT);
        permissions.add(PERMISSION_EXPENSE_VIEW);
        permissions.add(PERMISSION_BUDGET_VIEW);
        permissions.add(PERMISSION_REPORT_GENERATE);
        permissions.add(PERMISSION_REPORT_VIEW);
        permissions.add(PERMISSION_TAX_VIEW);
        return permissions;
    }

    public static Set<String> getDepartmentHeadPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add(PERMISSION_ACCOUNT_VIEW);
        permissions.add(PERMISSION_INCOME_VIEW);
        permissions.add(PERMISSION_EXPENSE_CREATE);
        permissions.add(PERMISSION_EXPENSE_VIEW);
        permissions.add(PERMISSION_BUDGET_CREATE);
        permissions.add(PERMISSION_BUDGET_VIEW);
        permissions.add(PERMISSION_REPORT_VIEW);
        return permissions;
    }

    public static Set<String> getQueryUserPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add(PERMISSION_ACCOUNT_VIEW);
        permissions.add(PERMISSION_INCOME_VIEW);
        permissions.add(PERMISSION_EXPENSE_VIEW);
        permissions.add(PERMISSION_BUDGET_VIEW);
        permissions.add(PERMISSION_REPORT_VIEW);
        return permissions;
    }

    public interface IFinanceUserRepository {
        java.util.Optional<FinanceUser> findByUserId(String userId);
        java.util.Optional<FinanceUser> findByUsername(String username);
        java.util.List<FinanceUser> findByRoleCode(String roleCode);
        java.util.List<FinanceUser> findByDataScope(String dataScope);
        FinanceUser save(FinanceUser user);
    }

    public interface IFinanceRoleRepository {
        java.util.Optional<FinanceRole> findByRoleCode(String roleCode);
        FinanceRole save(FinanceRole role);
    }

    public interface FinanceResource {
        String getDataScope();
        Long getDepartmentId();
        Long getProjectId();
    }
}
