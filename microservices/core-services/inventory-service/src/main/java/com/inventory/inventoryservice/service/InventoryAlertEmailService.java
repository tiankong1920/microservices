package com.inventory.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAlertEmailService {

    private final JavaMailSender mailSender;

    private static final String FROM_EMAIL = "inventory-alerts@inventory-system.com";

    @Async
    public void sendLowStockAlert(String toEmail, String productName, Long productId,
                                   Long warehouseId, Integer currentQuantity, Integer threshold) {
        String subject = String.format("[库存预警] %s (ID: %d) 库存不足", productName, productId);
        String body = String.format("""
            库存预警通知
            =============

            产品: %s (ID: %d)
            仓库: %d
            当前库存: %d
            预警阈值: %d

            请及时补货。
            """, productName, productId, warehouseId, currentQuantity, threshold);

        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendCriticalStockAlert(String toEmail, String productName, Long productId,
                                       Long warehouseId, Integer currentQuantity, Integer threshold) {
        String subject = String.format("[紧急预警] %s (ID: %d) 库存严重不足!", productName, productId);
        String body = String.format("""
            紧急库存预警通知
            =================

            产品: %s (ID: %d)
            仓库: %d
            当前库存: %d
            严重阈值: %d

            ⚠️ 库存严重不足，请立即处理！
            """, productName, productId, warehouseId, currentQuantity, threshold);

        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendOutOfStockAlert(String toEmail, String productName, Long productId,
                                    Long warehouseId) {
        String subject = String.format("[缺货警告] %s (ID: %d) 已缺货!", productName, productId);
        String body = String.format("""
            缺货警告通知
            =============

            产品: %s (ID: %d)
            仓库: %d
            当前库存: 0

            🚨 产品已缺货，请立即补货！
            """, productName, productId, warehouseId);

        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendReorderAlert(String toEmail, String productName, Long productId,
                                  Long warehouseId, Integer currentQuantity, Integer reorderPoint) {
        String subject = String.format("[补货提醒] %s (ID: %d) 达到补货点", productName, productId);
        String body = String.format("""
            补货提醒通知
            =============

            产品: %s (ID: %d)
            仓库: %d
            当前库存: %d
            补货点: %d

            请及时安排补货。
            """, productName, productId, warehouseId, currentQuantity, reorderPoint);

        sendEmail(toEmail, subject, body);
    }

    @Async
    public void sendBatchAlertSummary(String toEmail, int lowStockCount, int criticalCount,
                                      int outOfStockCount, int reorderCount) {
        String subject = String.format("[库存汇总] 低库存: %d | 严重: %d | 缺货: %d | 补货: %d",
                lowStockCount, criticalCount, outOfStockCount, reorderCount);
        String body = String.format("""
            库存预警汇总报告
            ===============

            低库存产品: %d 个
            严重库存不足: %d 个
            缺货产品: %d 个
            达到补货点: %d 个

            请登录系统查看详细清单。
            """, lowStockCount, criticalCount, outOfStockCount, reorderCount);

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Alert email sent successfully to: {}, subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send alert email to: {}, error: {}", to, e.getMessage());
        }
    }
}
