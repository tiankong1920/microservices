package com.inventory.datasourceservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.datasourceservice.dto.AlertConfigDTO;
import com.inventory.datasourceservice.entity.AlertConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    public boolean send(AlertConfig config, String message) {
        try {
            List<String> channels = objectMapper.readValue(config.getAlertChannels(), new TypeReference<>() {});
            AlertConfigDTO.AlertReceivers receivers = parseReceivers(config.getReceivers());
            
            boolean allSuccess = true;
            
            for (String channel : channels) {
                boolean success = switch (channel.toUpperCase()) {
                    case "EMAIL" -> sendEmail(receivers.getEmails(), config.getName(), message);
                    case "SMS" -> sendSms(receivers.getPhones(), message);
                    case "DINGTALK" -> sendDingtalk(receivers.getDingtalkWebhooks(), message);
                    case "WECHAT" -> sendWechat(receivers.getWechatWebhooks(), message);
                    default -> {
                        log.warn("Unknown channel: {}", channel);
                        yield false;
                    }
                };
                
                if (!success) {
                    allSuccess = false;
                }
            }
            
            return allSuccess;
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            return false;
        }
    }

    private boolean sendEmail(List<String> emails, String subject, String message) {
        if (emails == null || emails.isEmpty()) {
            log.warn("No email recipients configured");
            return false;
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(emails.toArray(new String[0]));
            mailMessage.setSubject("【数据源管理系统】" + subject);
            mailMessage.setText(message);
            
            mailSender.send(mailMessage);
            log.info("Email sent successfully to: {}", emails);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email", e);
            return false;
        }
    }

    private boolean sendSms(List<String> phones, String message) {
        if (phones == null || phones.isEmpty()) {
            log.warn("No SMS recipients configured");
            return false;
        }

        log.info("SMS would be sent to: {} with message: {}", phones, message);
        return true;
    }

    private boolean sendDingtalk(List<String> webhooks, String message) {
        if (webhooks == null || webhooks.isEmpty()) {
            log.warn("No DingTalk webhooks configured");
            return false;
        }

        for (String webhook : webhooks) {
            try {
                log.info("DingTalk notification would be sent to webhook: {} with message: {}", webhook, message);
            } catch (Exception e) {
                log.error("Failed to send DingTalk notification to: {}", webhook, e);
                return false;
            }
        }
        return true;
    }

    private boolean sendWechat(List<String> webhooks, String message) {
        if (webhooks == null || webhooks.isEmpty()) {
            log.warn("No WeChat webhooks configured");
            return false;
        }

        for (String webhook : webhooks) {
            try {
                log.info("WeChat notification would be sent to webhook: {} with message: {}", webhook, message);
            } catch (Exception e) {
                log.error("Failed to send WeChat notification to: {}", webhook, e);
                return false;
            }
        }
        return true;
    }

    private AlertConfigDTO.AlertReceivers parseReceivers(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return AlertConfigDTO.AlertReceivers.builder().build();
            }
            return objectMapper.readValue(json, AlertConfigDTO.AlertReceivers.class);
        } catch (Exception e) {
            log.error("Failed to parse receivers", e);
            return AlertConfigDTO.AlertReceivers.builder().build();
        }
    }
}
