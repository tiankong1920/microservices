package com.inventory.common.template.dto;

import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String templateCode;

    private String templateName;

    private String description;

    private BusinessDomain businessDomain;

    private String category;

    private TemplateStatus status;

    private String version;

    private Long parentVersionId;

    private List<TemplateFieldDTO> fields;

    private List<TemplateFieldGroupDTO> fieldGroups;

    private Map<String, Object> defaultValues;

    private Set<String> applicableScenarios;

    private Integer minConfigItems;

    private Integer maxConfigItems;

    private Boolean allowCustomFields;

    private Boolean allowExtension;

    private String extensionPoint;

    private Map<String, Object> metadata;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private String approvedBy;

    private LocalDateTime approvedAt;

    private LocalDateTime publishedAt;

    private LocalDateTime deprecatedAt;

    private String changeLog;

    private String tenantId;
}
