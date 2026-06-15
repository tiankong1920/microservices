package com.inventory.templateservice.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class CodeGenerator {

    private CodeGenerator() {
    }

    public static String generateTemplateCode(String businessDomain) {
        String prefix = "TPL";
        String domainCode = getDomainCode(businessDomain);
        String sequence = generateSequence();
        return String.format("%s_%s_%s", prefix, domainCode, sequence);
    }

    public static String generateFieldCode(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return "FIELD_" + generateShortId();
        }
        
        String sanitized = fieldName.toUpperCase()
                .replaceAll("[^A-Z0-9_]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
        
        if (sanitized.isEmpty()) {
            return "FIELD_" + generateShortId();
        }
        
        return sanitized;
    }

    public static String generateVersionCode() {
        return "V" + System.currentTimeMillis();
    }

    public static String generateImportBatchId() {
        return "IMP_" + generateShortId() + "_" + System.currentTimeMillis();
    }

    public static String generateExportBatchId() {
        return "EXP_" + generateShortId() + "_" + System.currentTimeMillis();
    }

    public static String generateShortId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + 
               String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private static String getDomainCode(String businessDomain) {
        if (businessDomain == null) {
            return "GEN";
        }
        
        return switch (businessDomain.toUpperCase()) {
            case "CUSTOMER_MANAGEMENT" -> "CUST";
            case "SUPPLIER_MANAGEMENT" -> "SUPP";
            case "INVENTORY_MANAGEMENT" -> "INV";
            case "ORDER_MANAGEMENT" -> "ORD";
            case "FINANCE_MANAGEMENT" -> "FIN";
            case "PROCUREMENT_MANAGEMENT" -> "PROC";
            case "SALES_MANAGEMENT" -> "SALE";
            case "REPORT_MANAGEMENT" -> "RPT";
            default -> businessDomain.length() >= 4 
                    ? businessDomain.substring(0, 4).toUpperCase() 
                    : businessDomain.toUpperCase();
        };
    }

    private static String generateSequence() {
        return String.format("%03d", ThreadLocalRandom.current().nextInt(1, 1000));
    }

    public static String generateValidationRuleCode(String ruleName) {
        if (ruleName == null || ruleName.isEmpty()) {
            return "RULE_" + generateShortId();
        }
        
        String sanitized = ruleName.toUpperCase()
                .replaceAll("[^A-Z0-9_]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
        
        return "RULE_" + sanitized;
    }

    public static String generateAuditLogId() {
        return "AUDIT_" + System.currentTimeMillis() + "_" + generateShortId();
    }
}
