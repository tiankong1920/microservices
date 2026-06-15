package com.inventory.adminservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "password.policy")
public class PasswordPolicyConfig {

    private int minLength = 12;
    private int maxLength = 64;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireDigit = true;
    private boolean requireSpecialChar = true;
    private String specialChars = "!@#$%^&*";
    private int maxLoginAttempts = 5;
    private int lockoutDurationMinutes = 15;
    private int passwordHistorySize = 5;

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isRequireUppercase() {
        return requireUppercase;
    }

    public void setRequireUppercase(boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }

    public boolean isRequireLowercase() {
        return requireLowercase;
    }

    public void setRequireLowercase(boolean requireLowercase) {
        this.requireLowercase = requireLowercase;
    }

    public boolean isRequireDigit() {
        return requireDigit;
    }

    public void setRequireDigit(boolean requireDigit) {
        this.requireDigit = requireDigit;
    }

    public boolean isRequireSpecialChar() {
        return requireSpecialChar;
    }

    public void setRequireSpecialChar(boolean requireSpecialChar) {
        this.requireSpecialChar = requireSpecialChar;
    }

    public String getSpecialChars() {
        return specialChars;
    }

    public void setSpecialChars(String specialChars) {
        this.specialChars = specialChars;
    }

    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public void setMaxLoginAttempts(int maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }

    public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    public int getPasswordHistorySize() {
        return passwordHistorySize;
    }

    public void setPasswordHistorySize(int passwordHistorySize) {
        this.passwordHistorySize = passwordHistorySize;
    }
}
