package com.inventory.common.template.dto;

import com.inventory.common.template.FieldPermission;
import com.inventory.common.template.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFieldDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String fieldCode;

    private String fieldName;

    private String fieldLabel;

    private FieldType fieldType;

    private String description;

    private String placeholder;

    private String defaultValue;

    private Boolean required;

    private Boolean unique;

    private Boolean searchable;

    private Boolean sortable;

    private Integer displayOrder;

    private String validationRegex;

    private Integer minLength;

    private Integer maxLength;

    private Integer minValue;

    private Integer maxValue;

    private Integer precision;

    private Integer scale;

    private List<String> options;

    private Map<String, Object> extraConfig;

    private FieldPermission defaultPermission;

    private Map<String, FieldPermission> rolePermissions;

    private Boolean sensitive;

    private String groupId;

    private String dependencyField;

    private String dependencyValue;
}
