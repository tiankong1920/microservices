package com.inventory.authservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 128;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    public PasswordValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty");
            return new PasswordValidationResult(false, errors);
        }

        if (password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (password.length() > MAX_LENGTH) {
            errors.add("Password must not exceed " + MAX_LENGTH + " characters");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one digit");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one special character");
        }

        boolean valid = errors.isEmpty();
        return new PasswordValidationResult(valid, errors);
    }

    public static class PasswordValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public PasswordValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getViolationMessage() {
            return errors.isEmpty() ? "密码验证失败" : String.join("; ", errors);
        }
    }
}
