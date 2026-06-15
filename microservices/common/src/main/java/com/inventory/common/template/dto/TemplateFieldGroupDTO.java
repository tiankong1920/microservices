package com.inventory.common.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFieldGroupDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String groupId;

    private String groupName;

    private String groupLabel;

    private String description;

    private Integer displayOrder;

    private Boolean collapsible;

    private Boolean defaultCollapsed;

    private List<String> fieldCodes;
}
