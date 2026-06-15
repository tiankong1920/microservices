package com.inventory.templateservice.security;

import com.inventory.common.template.TemplateStatus;
import com.inventory.common.template.exception.TemplateException;
import com.inventory.templateservice.entity.Template;
import com.inventory.templateservice.repository.ITemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionService {

    private final ITemplateRepository templateRepository;

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = new LinkedHashMap<>();

    static {
        ROLE_PERMISSIONS.put(PermissionConstants.ROLE_TEMPLATE_ADMIN, Set.of(
                PermissionConstants.TEMPLATE_VIEW,
                PermissionConstants.TEMPLATE_CREATE,
                PermissionConstants.TEMPLATE_EDIT,
                PermissionConstants.TEMPLATE_DELETE,
                PermissionConstants.TEMPLATE_PUBLISH,
                PermissionConstants.TEMPLATE_APPROVE,
                PermissionConstants.TEMPLATE_ARCHIVE,
                PermissionConstants.TEMPLATE_ROLLBACK,
                PermissionConstants.TEMPLATE_MANAGE_FIELDS,
                PermissionConstants.TEMPLATE_MANAGE_CUSTOM_FIELDS,
                PermissionConstants.TEMPLATE_EXPORT,
                PermissionConstants.TEMPLATE_IMPORT,
                PermissionConstants.TEMPLATE_MANAGE_VERSIONS,
                PermissionConstants.TEMPLATE_VIEW_AUDIT_LOGS
        ));

        ROLE_PERMISSIONS.put(PermissionConstants.ROLE_TEMPLATE_EDITOR, Set.of(
                PermissionConstants.TEMPLATE_VIEW,
                PermissionConstants.TEMPLATE_CREATE,
                PermissionConstants.TEMPLATE_EDIT,
                PermissionConstants.TEMPLATE_MANAGE_FIELDS,
                PermissionConstants.TEMPLATE_MANAGE_CUSTOM_FIELDS,
                PermissionConstants.TEMPLATE_EXPORT,
                PermissionConstants.TEMPLATE_IMPORT
        ));

        ROLE_PERMISSIONS.put(PermissionConstants.ROLE_TEMPLATE_VIEWER, Set.of(
                PermissionConstants.TEMPLATE_VIEW,
                PermissionConstants.TEMPLATE_EXPORT
        ));

        ROLE_PERMISSIONS.put(PermissionConstants.ROLE_TEMPLATE_APPROVER, Set.of(
                PermissionConstants.TEMPLATE_VIEW,
                PermissionConstants.TEMPLATE_APPROVE,
                PermissionConstants.TEMPLATE_PUBLISH,
                PermissionConstants.TEMPLATE_VIEW_AUDIT_LOGS
        ));

        ROLE_PERMISSIONS.put(PermissionConstants.ROLE_TEMPLATE_AUDITOR, Set.of(
                PermissionConstants.TEMPLATE_VIEW,
                PermissionConstants.TEMPLATE_VIEW_AUDIT_LOGS,
                PermissionConstants.TEMPLATE_MANAGE_VERSIONS
        ));
    }

    public TemplatePermission getPermission(Long templateId, String userId, Set<String> roles) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> TemplateException.notFound(templateId));

        return buildPermission(template, userId, roles);
    }

    public TemplatePermission getPermission(String templateCode, String userId, Set<String> roles) {
        Template template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> TemplateException.notFoundByCode(templateCode));

        return buildPermission(template, userId, roles);
    }

    public boolean hasPermission(Long templateId, String userId, Set<String> roles, String permission) {
        TemplatePermission perm = getPermission(templateId, userId, roles);
        return perm.getPermissions().contains(permission);
    }

    public boolean hasPermission(String templateCode, String userId, Set<String> roles, String permission) {
        TemplatePermission perm = getPermission(templateCode, userId, roles);
        return perm.getPermissions().contains(permission);
    }

    public void checkPermission(Long templateId, String userId, Set<String> roles, String permission) {
        if (!hasPermission(templateId, userId, roles, permission)) {
            throw TemplateException.permissionDenied(permission, templateId);
        }
    }

    public void checkPermission(String templateCode, String userId, Set<String> roles, String permission) {
        if (!hasPermission(templateCode, userId, roles, permission)) {
            throw TemplateException.permissionDenied(permission, 
                    templateRepository.findByTemplateCode(templateCode)
                            .map(Template::getId)
                            .orElse(-1L));
        }
    }

    public void checkCanModify(Long templateId, String userId, Set<String> roles) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> TemplateException.notFound(templateId));

        if (template.getStatus() == TemplateStatus.PUBLISHED) {
            if (!hasPermission(templateId, userId, roles, PermissionConstants.TEMPLATE_APPROVE)) {
                throw TemplateException.cannotModify(templateId, "Published template requires approver permission to modify");
            }
        } else {
            checkPermission(templateId, userId, roles, PermissionConstants.TEMPLATE_EDIT);
        }
    }

    public void checkCanPublish(Long templateId, String userId, Set<String> roles) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> TemplateException.notFound(templateId));

        if (template.getStatus() != TemplateStatus.APPROVED) {
            throw TemplateException.cannotModify(templateId, "Only approved templates can be published");
        }

        checkPermission(templateId, userId, roles, PermissionConstants.TEMPLATE_PUBLISH);
    }

    public Set<String> getPermissionsForRoles(Set<String> roles) {
        Set<String> permissions = new HashSet<>();
        for (String role : roles) {
            Set<String> rolePerms = ROLE_PERMISSIONS.get(role);
            if (rolePerms != null) {
                permissions.addAll(rolePerms);
            }
        }
        return permissions;
    }

    public Map<String, Set<String>> getAllRolePermissions() {
        return Collections.unmodifiableMap(ROLE_PERMISSIONS);
    }

    public Set<String> getStandardRoles() {
        return ROLE_PERMISSIONS.keySet();
    }

    private TemplatePermission buildPermission(Template template, String userId, Set<String> roles) {
        Set<String> permissions = getPermissionsForRoles(roles);

        return TemplatePermission.builder()
                .templateId(template.getId())
                .templateCode(template.getTemplateCode())
                .userId(userId)
                .roles(roles)
                .permissions(permissions)
                .canView(permissions.contains(PermissionConstants.TEMPLATE_VIEW))
                .canCreate(permissions.contains(PermissionConstants.TEMPLATE_CREATE))
                .canEdit(permissions.contains(PermissionConstants.TEMPLATE_EDIT))
                .canDelete(permissions.contains(PermissionConstants.TEMPLATE_DELETE))
                .canPublish(permissions.contains(PermissionConstants.TEMPLATE_PUBLISH))
                .canApprove(permissions.contains(PermissionConstants.TEMPLATE_APPROVE))
                .canArchive(permissions.contains(PermissionConstants.TEMPLATE_ARCHIVE))
                .canRollback(permissions.contains(PermissionConstants.TEMPLATE_ROLLBACK))
                .canManageFields(permissions.contains(PermissionConstants.TEMPLATE_MANAGE_FIELDS))
                .canManageCustomFields(permissions.contains(PermissionConstants.TEMPLATE_MANAGE_CUSTOM_FIELDS))
                .canExport(permissions.contains(PermissionConstants.TEMPLATE_EXPORT))
                .canImport(permissions.contains(PermissionConstants.TEMPLATE_IMPORT))
                .build();
    }
}
