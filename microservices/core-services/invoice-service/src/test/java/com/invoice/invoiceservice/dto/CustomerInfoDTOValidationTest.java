package com.invoice.invoiceservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerInfoDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCustomerInfo() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("北京科技有限公司")
                .taxNumber("91110000MA01B1234X")
                .registeredAddress("北京市朝阳区")
                .contactPhone("010-12345678")
                .mobilePhone("13800138000")
                .bankAccount("6222021234567890123")
                .email("test@example.com")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmptyName() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("")
                .taxNumber("91110000MA01B1234X")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerName")));
    }

    @Test
    void testInvalidTaxNumber() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("abc")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("taxNumber")));
    }

    @Test
    void testShortTaxNumber() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("12345678901234")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidTaxNumber15Digits() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("123456789012345")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidTaxNumber20Digits() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("91110000MA01B1234XY")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidMobilePhone() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("91110000MA01B1234X")
                .mobilePhone("12345")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidMobilePhone() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("91110000MA01B1234X")
                .mobilePhone("13800138000")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidContactPhone() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("91110000MA01B1234X")
                .contactPhone("12345678")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidContactPhone() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("91110000MA01B1234X")
                .contactPhone("010-12345678")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidEmail() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("91110000MA01B1234X")
                .email("invalid-email")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidBankAccount() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("91110000MA01B1234X")
                .bankAccount("中文账号")
                .build();

        Set<ConstraintViolation<CustomerInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    private void assertFalse(boolean condition) {
        org.junit.jupiter.api.Assertions.assertFalse(condition);
    }
}
