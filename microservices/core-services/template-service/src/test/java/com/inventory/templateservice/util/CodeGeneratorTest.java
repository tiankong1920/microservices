package com.inventory.templateservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorTest {

    @Test
    @DisplayName("生成模板编码")
    void generateTemplateCode() {
        String code1 = CodeGenerator.generateTemplateCode("CUSTOMER_MANAGEMENT");
        assertTrue(code1.startsWith("TPL_CUST_"));
        assertEquals(12, code1.length());

        String code2 = CodeGenerator.generateTemplateCode("INVENTORY_MANAGEMENT");
        assertTrue(code2.startsWith("TPL_INV_"));

        String code3 = CodeGenerator.generateTemplateCode("SUPPLIER_MANAGEMENT");
        assertTrue(code3.startsWith("TPL_SUPP_"));

        String code4 = CodeGenerator.generateTemplateCode(null);
        assertTrue(code4.startsWith("TPL_GEN_"));
    }

    @Test
    @DisplayName("生成字段编码")
    void generateFieldCode() {
        String code1 = CodeGenerator.generateFieldCode("Customer Name");
        assertEquals("CUSTOMER_NAME", code1);

        String code2 = CodeGenerator.generateFieldCode("email_address");
        assertEquals("EMAIL_ADDRESS", code2);

        String code3 = CodeGenerator.generateFieldCode("Phone#Number");
        assertEquals("PHONE_NUMBER", code3);

        String code4 = CodeGenerator.generateFieldCode(null);
        assertTrue(code4.startsWith("FIELD_"));

        String code5 = CodeGenerator.generateFieldCode("");
        assertTrue(code5.startsWith("FIELD_"));
    }

    @Test
    @DisplayName("生成版本编码")
    void generateVersionCode() {
        String code = CodeGenerator.generateVersionCode();
        assertTrue(code.startsWith("V"));
        assertTrue(code.length() > 1);
    }

    @Test
    @DisplayName("生成导入批次ID")
    void generateImportBatchId() {
        String id = CodeGenerator.generateImportBatchId();
        assertTrue(id.startsWith("IMP_"));
        assertTrue(id.contains("_"));
    }

    @Test
    @DisplayName("生成导出批次ID")
    void generateExportBatchId() {
        String id = CodeGenerator.generateExportBatchId();
        assertTrue(id.startsWith("EXP_"));
        assertTrue(id.contains("_"));
    }

    @Test
    @DisplayName("生成短ID")
    void generateShortId() {
        String id1 = CodeGenerator.generateShortId();
        String id2 = CodeGenerator.generateShortId();
        
        assertEquals(8, id1.length());
        assertEquals(8, id2.length());
        assertNotEquals(id1, id2);
        assertTrue(id1.matches("[A-Z0-9]+"));
    }

    @Test
    @DisplayName("生成事务ID")
    void generateTransactionId() {
        String txnId = CodeGenerator.generateTransactionId();
        assertTrue(txnId.startsWith("TXN_"));
        assertTrue(txnId.contains("_"));
    }

    @Test
    @DisplayName("生成校验规则编码")
    void generateValidationRuleCode() {
        String code1 = CodeGenerator.generateValidationRuleCode("Email Format");
        assertEquals("RULE_EMAIL_FORMAT", code1);

        String code2 = CodeGenerator.generateValidationRuleCode("phone_number");
        assertEquals("RULE_PHONE_NUMBER", code2);

        String code3 = CodeGenerator.generateValidationRuleCode(null);
        assertTrue(code3.startsWith("RULE_"));

        String code4 = CodeGenerator.generateValidationRuleCode("");
        assertTrue(code4.startsWith("RULE_"));
    }

    @Test
    @DisplayName("生成审计日志ID")
    void generateAuditLogId() {
        String id = CodeGenerator.generateAuditLogId();
        assertTrue(id.startsWith("AUDIT_"));
        assertTrue(id.contains("_"));
    }
}
