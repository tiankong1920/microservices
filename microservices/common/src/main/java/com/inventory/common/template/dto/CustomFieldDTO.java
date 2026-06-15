package com.inventory.common.template.dto;

import com.inventory.common.template.FieldType;
import com.inventory.common.template.FieldPermission;
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
public class CustomFieldDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long templateId;

    private String fieldCode;

    private String fieldName;

    private String fieldLabel;

    private FieldType fieldType;

    private String description;

    private String defaultValue;

    private Boolean required;

    private String validationRegex;

    private Integer minLength;

    private Integer maxLength;

    private Integer minValue;

    private Integer maxValue;

    private List<String> options;

    private Map<String, FieldPermission> rolePermissions;

    private Boolean active;

    private String createdBy;

    private String updatedBy;
}
