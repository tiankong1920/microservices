package com.inventory.datasourceservice.dto;

import com.inventory.datasourceservice.entity.DatasourceConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigTemplateDTO {

    private Long id;

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 128, message = "模板名称长度不能超过128个字符")
    private String name;

    @NotNull(message = "数据源类型不能为空")
    private DatasourceConfig.DatasourceType type;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @NotBlank(message = "配置内容不能为空")
    private String configJson;

    private Boolean isPublic;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
