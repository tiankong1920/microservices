package com.inventory.adminservice.service;

import com.inventory.adminservice.config.PasswordPolicyConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PasswordStrengthServiceTest {

    @Mock
    private PasswordPolicyConfig passwordPolicyConfig;

    private PasswordStrengthService passwordStrengthService;

    @BeforeEach
    void setUp() {
        passwordStrengthService = new PasswordStrengthService(passwordPolicyConfig);
        when(passwordPolicyConfig.getMinLength()).thenReturn(8);
        when(passwordPolicyConfig.getMaxLength()).thenReturn(64);
        when(passwordPolicyConfig.isRequireUppercase()).thenReturn(true);
        when(passwordPolicyConfig.isRequireLowercase()).thenReturn(true);
        when(passwordPolicyConfig.isRequireDigit()).thenReturn(true);
        when(passwordPolicyConfig.isRequireSpecialChar()).thenReturn(true);
        when(passwordPolicyConfig.getSpecialChars()).thenReturn("!@#$%^&*");
    }

    @Test
    void testValidatePassword_StrongPassword() {
        String password = "StrongP@ssw0rd123";
        var result = passwordStrengthService.validatePassword(password);

        assertTrue(result.isValid());
        assertTrue(result.score() >= 70);
        assertEquals("强", result.getStrengthLabel());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void testValidatePassword_WeakPassword() {
        String password = "weak";
        var result = passwordStrengthService.validatePassword(password);

        assertFalse(result.isValid());
        assertTrue(result.score() < 40);
        assertEquals("弱", result.getStrengthLabel());
        assertFalse(result.errors().isEmpty());
    }

    @Test
    void testValidatePassword_MissingUppercase() {
        String password = "lowercase123!@";
        var result = passwordStrengthService.validatePassword(password);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("大写字母")));
    }

    @Test
    void testValidatePassword_MissingLowercase() {
        String password = "UPPERCASE123!@";
        var result = passwordStrengthService.validatePassword(password);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("小写字母")));
    }

    @Test
    void testValidatePassword_MissingDigit() {
        String password = "NoDigits!@Abc";
        var result = passwordStrengthService.validatePassword(password);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("数字")));
    }

    @Test
    void testValidatePassword_MissingSpecialChar() {
        String password = "NoSpecial123Abc";
        var result = passwordStrengthService.validatePassword(password);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("特殊字符")));
    }

    @Test
    void testValidatePassword_TooShort() {
        String password = "Short1!";
        var result = passwordStrengthService.validatePassword(password);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("密码长度至少")));
    }

    @Test
    void testValidatePassword_TooLong() {
        String password = "A".repeat(65);
        var result = passwordStrengthService.validatePassword(password);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("密码长度不能超过")));
    }

    @Test
    void testValidatePassword_EmptyPassword() {
        String password = "";
        var result = passwordStrengthService.validatePassword(password);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("密码不能为空")));
    }

    @Test
    void testValidatePassword_NullPassword() {
        String password = null;
        var result = passwordStrengthService.validatePassword(password);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("密码不能为空")));
    }

    @Test
    void testGetPasswordImprovementSuggestions() {
        String password = "weak";
        var suggestions = passwordStrengthService.getPasswordImprovementSuggestions(password);

        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("长度") || s.contains("位")));
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("大写字母")));
    }

    @Test
    void testGetPasswordImprovementSuggestions_StrongPassword() {
        String password = "StrongP@ssw0rd123";
        var suggestions = passwordStrengthService.getPasswordImprovementSuggestions(password);

        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("密码强度良好") || s.contains("建议")));
    }
}
