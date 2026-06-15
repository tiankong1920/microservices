package com.inventory.datasourceservice.dto;

import com.inventory.datasourceservice.entity.DatasourceConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class DatasourceConfigDTO {

    private Long id;

    @NotBlank(message = "数据源名称不能为空")
    @Size(max = 128, message = "数据源名称长度不能超过128个字符")
    private String name;

    @NotNull(message = "数据源类型不能为空")
    private DatasourceConfig.DatasourceType type;

    @Size(max = 32, message = "版本号长度不能超过32个字符")
    private String version;

    @NotBlank(message = "主机地址不能为空")
    @Size(max = 255, message = "主机地址长度不能超过255个字符")
    private String host;

    @NotNull(message = "端口不能为空")
    @Min(value = 1, message = "端口范围应在1-65535之间")
    @Max(value = 65535, message = "端口范围应在1-65535之间")
    private Integer port;

    @Size(max = 128, message = "数据库名长度不能超过128个字符")
    private String databaseName;

    @Size(max = 128, message = "用户名长度不能超过128个字符")
    private String username;

    @Size(max = 512, message = "密码长度不能超过512个字符")
    private String password;

    private String extraConfig;

    private DatasourceConfig.DatasourceStatus status;

    private Long templateId;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private ConnectionStatusDTO connectionStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionStatusDTO {
        private String status;
        private Integer responseTime;
        private String errorMessage;
        private LocalDateTime checkedAt;
    }
}
