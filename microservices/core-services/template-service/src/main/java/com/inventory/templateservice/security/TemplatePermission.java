package com.inventory.templateservice.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePermission {

    private Long templateId;
    private String templateCode;
    private String userId;
    private Set<String> roles;
    private Set<String> permissions;
    private boolean canView;
    private boolean canCreate;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canPublish;
    private boolean canApprove;
    private boolean canArchive;
    private boolean canRollback;
    private boolean canManageFields;
    private boolean canManageCustomFields;
    private boolean canExport;
    private boolean canImport;
}
