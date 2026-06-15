package com.inventory.adminservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionDTO {
    private Long id;
    private String permissionName;
    private String permissionCode;
    private String resourceType;
    private String resourcePath;
    private String method;
    private String description;
}
