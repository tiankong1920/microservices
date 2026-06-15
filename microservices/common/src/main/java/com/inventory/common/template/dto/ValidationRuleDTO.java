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
public class ValidationRuleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String ruleCode;

    private String ruleName;

    private String ruleType;

    private String regexPattern;

    private String errorMessage;

    private Map<String, Object> parameters;

    private List<String> applicableFieldTypes;

    private Boolean builtIn;

    private Boolean active;

    private String createdBy;

    private LocalDateTime createdAt;
}
