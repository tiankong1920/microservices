/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.authservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordPolicyValidatorTest {

    private PasswordPolicyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordPolicyValidator();
    }

    @Test
    @DisplayName("Valid password should pass validation")
    void testValidPassword() {
        PasswordPolicyValidator.PasswordValidationResult result = validator.validate("Password123!");
        assertTrue(result.isValid(), "12+ char password with uppercase, lowercase, digit, special should pass");
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Password without uppercase should fail")
    void testPasswordWithoutUppercase() {
        PasswordPolicyValidator.PasswordValidationResult result = validator.validate("password123!");
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Password without lowercase should fail")
    void testPasswordWithoutLowercase() {
        PasswordPolicyValidator.PasswordValidationResult result = validator.validate("PASSWORD123!");
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Password without digit should fail")
    void testPasswordWithoutDigit() {
        PasswordPolicyValidator.PasswordValidationResult result = validator.validate("Password!");
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Password without special character should fail")
    void testPasswordWithoutSpecialChar() {
        PasswordPolicyValidator.PasswordValidationResult result = validator.validate("Password123");
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("11-character password should fail (below 12 minimum)")
    void testElevenCharPassword() {
        PasswordPolicyValidator.PasswordValidationResult result = validator.validate("Password12!");
        assertFalse(result.isValid(), "11 char password should fail with new 12 char minimum");
    }

    @Test
    @DisplayName("Empty password should fail")
    void testEmptyPassword() {
        PasswordPolicyValidator.PasswordValidationResult result = validator.validate("");
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Null password should fail")
    void testNullPassword() {
        PasswordPolicyValidator.PasswordValidationResult result = validator.validate(null);
        assertFalse(result.isValid());
    }
}
