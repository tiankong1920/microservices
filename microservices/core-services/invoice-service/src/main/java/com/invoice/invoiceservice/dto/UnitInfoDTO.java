package com.invoice.invoiceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitInfoDTO {

    private Long id;

    @NotBlank(message = "单位名称不能为空")
    @Size(max = 50, message = "单位名称长度不能超过50个字符")
    private String unitName;

    @Size(max = 500, message = "单位别名长度不能超过500个字符")
    private String aliases;

    private Integer usageCount;

    private String tenantId;
}
