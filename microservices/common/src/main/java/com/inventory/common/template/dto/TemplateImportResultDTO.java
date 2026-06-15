package com.inventory.common.template.dto;

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
public class TemplateImportResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean success;

    private Integer totalCount;

    private Integer successCount;

    private Integer failedCount;

    private Integer skippedCount;

    private List<TemplateImportErrorDTO> errors;

    private List<String> importedTemplateCodes;

    private Map<String, Object> summary;
}
