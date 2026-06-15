package com.inventory.datasourceservice.dto;

import com.inventory.datasourceservice.entity.AlertConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertConfigDTO {

    private Long id;

    @NotBlank(message = "告警名称不能为空")
    @Size(max = 128, message = "告警名称长度不能超过128个字符")
    private String name;

    private List<Long> datasourceIds;

    @NotNull(message = "告警级别不能为空")
    private AlertConfig.AlertLevel alertLevel;

    @NotNull(message = "告警渠道不能为空")
    private List<String> alertChannels;

    @NotNull(message = "接收人配置不能为空")
    private AlertReceivers receivers;

    private AlertConfig.NotifyFrequency notifyFrequency;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertReceivers {
        private List<String> emails;
        private List<String> phones;
        private List<String> dingtalkWebhooks;
        private List<String> wechatWebhooks;
    }
}
