package com.inventory.common.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVersionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long templateId;

    private String templateCode;

    private String versionNumber;

    private Integer majorVersion;

    private Integer minorVersion;

    private Integer patchVersion;

    private String changeDescription;

    private String changeType;

    private List<String> changedFields;

    private Map<String, Object> snapshot;

    private String changedBy;

    private LocalDateTime changedAt;

    private String changeReason;

    private Boolean isRollback;

    private Long rollbackFromVersionId;
}
