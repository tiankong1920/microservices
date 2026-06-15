package com.inventory.adminservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RoleDTO {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Set<PermissionDTO> permissions;
}
