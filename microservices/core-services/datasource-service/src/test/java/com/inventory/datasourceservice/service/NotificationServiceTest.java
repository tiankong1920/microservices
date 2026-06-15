package com.inventory.datasourceservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.datasourceservice.entity.AlertConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private NotificationService notificationService;

    private AlertConfig testAlertConfig;

    @BeforeEach
    void setUp() {
        testAlertConfig = AlertConfig.builder()
                .id(1L)
                .tenantId("tenant-001")
                .datasourceIds("[1]")
                .name("Test Alert")
                .alertChannels("[\"EMAIL\",\"SMS\"]")
                .receivers("{\"emails\":[\"test@example.com\"],\"phones\":[\"13800138000\"]}")
                .build();

        notificationService = new NotificationService(mailSender, objectMapper);
    }

    @Nested
    @DisplayName("send() method tests")
    class SendTests {

        @Test
        @DisplayName("should send notification successfully")
        void shouldSendNotificationSuccessfully() throws Exception {
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            boolean result = notificationService.send(testAlertConfig, "Test message");

            assertTrue(result);
            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("should return false when exception occurs")
        void shouldReturnFalseWhenExceptionOccurs() throws Exception {
            AlertConfig badConfig = AlertConfig.builder()
                    .alertChannels("invalid json")
                    .build();

            boolean result = notificationService.send(badConfig, "Test message");

            assertFalse(result);
        }

        @Test
        @DisplayName("should handle unknown channel")
        void shouldHandleUnknownChannel() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"UNKNOWN_CHANNEL\"]")
                    .receivers("{}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertFalse(result);
        }

        @Test
        @DisplayName("should handle empty channels")
        void shouldHandleEmptyChannels() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[]")
                    .receivers("{}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Email notification tests")
    class EmailNotificationTests {

        @Test
        @DisplayName("should send email successfully")
        void shouldSendEmailSuccessfully() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"EMAIL\"]")
                    .receivers("{\"emails\":[\"user1@example.com\",\"user2@example.com\"]}")
                    .build();

            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            boolean result = notificationService.send(config, "Test message");

            assertTrue(result);
            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("should return false when no email recipients")
        void shouldReturnFalseWhenNoEmailRecipients() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"EMAIL\"]")
                    .receivers("{\"emails\":[]}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertFalse(result);
        }

        @Test
        @DisplayName("should handle email send failure")
        void shouldHandleEmailSendFailure() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"EMAIL\"]")
                    .receivers("{\"emails\":[\"test@example.com\"]}")
                    .build();

            doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

            boolean result = notificationService.send(config, "Test message");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("SMS notification tests")
    class SmsNotificationTests {

        @Test
        @DisplayName("should send SMS successfully")
        void shouldSendSmsSuccessfully() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"SMS\"]")
                    .receivers("{\"phones\":[\"13800138000\"]}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertTrue(result);
        }

        @Test
        @DisplayName("should return false when no phone numbers")
        void shouldReturnFalseWhenNoPhoneNumbers() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"SMS\"]")
                    .receivers("{\"phones\":[]}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("DingTalk notification tests")
    class DingTalkNotificationTests {

        @Test
        @DisplayName("should send DingTalk notification successfully")
        void shouldSendDingTalkNotificationSuccessfully() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"DINGTALK\"]")
                    .receivers("{\"dingtalkWebhooks\":[\"https://oapi.dingtalk.com/robot/send?access_token=xxx\"]}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertTrue(result);
        }

        @Test
        @DisplayName("should return false when no DingTalk webhooks")
        void shouldReturnFalseWhenNoDingTalkWebhooks() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"DINGTALK\"]")
                    .receivers("{\"dingtalkWebhooks\":[]}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("WeChat notification tests")
    class WeChatNotificationTests {

        @Test
        @DisplayName("should send WeChat notification successfully")
        void shouldSendWeChatNotificationSuccessfully() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"WECHAT\"]")
                    .receivers("{\"wechatWebhooks\":[\"https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx\"]}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertTrue(result);
        }

        @Test
        @DisplayName("should return false when no WeChat webhooks")
        void shouldReturnFalseWhenNoWeChatWebhooks() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[\"WECHAT\"]")
                    .receivers("{\"wechatWebhooks\":[]}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("parseReceivers() method tests")
    class ParseReceiversTests {

        @Test
        @DisplayName("should parse valid JSON receivers")
        void shouldParseValidJsonReceivers() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[]")
                    .receivers("{\"emails\":[\"test@example.com\"],\"phones\":[\"13800138000\"]}")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertTrue(result);
        }

        @Test
        @DisplayName("should handle null receivers")
        void shouldHandleNullReceivers() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[]")
                    .receivers(null)
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertTrue(result);
        }

        @Test
        @DisplayName("should handle empty receivers")
        void shouldHandleEmptyReceivers() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[]")
                    .receivers("")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertTrue(result);
        }

        @Test
        @DisplayName("should handle invalid JSON receivers")
        void shouldHandleInvalidJsonReceivers() throws Exception {
            AlertConfig config = AlertConfig.builder()
                    .alertChannels("[]")
                    .receivers("invalid json")
                    .build();

            boolean result = notificationService.send(config, "Test message");

            assertTrue(result);
        }
    }
}
