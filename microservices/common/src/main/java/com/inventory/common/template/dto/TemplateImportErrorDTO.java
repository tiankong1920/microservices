package com.inventory.common.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateImportErrorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer rowIndex;

    private String templateCode;

    private String fieldName;

    private String errorType;

    private String errorMessage;

    private String suggestion;
}
