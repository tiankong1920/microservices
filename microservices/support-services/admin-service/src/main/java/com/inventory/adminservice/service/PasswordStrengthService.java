package com.inventory.adminservice.service;

import com.inventory.adminservice.config.PasswordPolicyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordStrengthService {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern MULTIPLE_UPPERCASE = Pattern.compile("[A-Z].*[A-Z]");
    private static final Pattern MULTIPLE_LOWERCASE = Pattern.compile("[a-z].*[a-z]");
    private static final Pattern MULTIPLE_DIGIT = Pattern.compile("[0-9].*[0-9]");

    private final PasswordPolicyConfig passwordPolicyConfig;

    public enum PasswordStrength {
        WEAK("弱", 0, 40),
        MEDIUM("中", 40, 70),
        STRONG("强", 70, 100);

        private final String label;
        private final int minScore;
        @SuppressWarnings("unused")
        private final int maxScore;

        PasswordStrength(String label, int minScore, @SuppressWarnings("unused") int maxScore) {
            this.label = label;
            this.minScore = minScore;
            this.maxScore = maxScore;
        }

        public String getLabel() {
            return label;
        }

        public static PasswordStrength fromScore(int score) {
            if (score >= STRONG.minScore) {
                return STRONG;
            } else if (score >= MEDIUM.minScore) {
                return MEDIUM;
            } else {
                return WEAK;
            }
        }
    }

    public PasswordValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordValidationResult(false, 0, PasswordStrength.WEAK, List.of("密码不能为空"));
        }

        List<String> errors = new ArrayList<>();
        PasswordScore score = new PasswordScore();

        validateLength(password, errors, score);
        validateUppercase(password, errors, score);
        validateLowercase(password, errors, score);
        validateDigit(password, errors, score);
        validateSpecialChar(password, errors, score);
        addBonusScore(password, score);

        PasswordStrength strength = PasswordStrength.fromScore(score.getValue());
        return new PasswordValidationResult(errors.isEmpty(), score.getValue(), strength, errors);
    }

    private void validateLength(String password, List<String> errors, PasswordScore score) {
        int minLen = passwordPolicyConfig.getMinLength();
        int maxLen = passwordPolicyConfig.getMaxLength();

        if (password.length() < minLen) {
            errors.add("密码长度至少" + minLen + "位");
        } else if (password.length() > maxLen) {
            errors.add("密码长度不能超过" + maxLen + "位");
        } else {
            score.add(10);
        }
    }

    private void validateUppercase(String password, List<String> errors, PasswordScore score) {
        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        if (passwordPolicyConfig.isRequireUppercase() && !hasUppercase) {
            errors.add("密码必须包含大写字母");
        } else if (hasUppercase) {
            score.add(20);
        }
    }

    private void validateLowercase(String password, List<String> errors, PasswordScore score) {
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        if (passwordPolicyConfig.isRequireLowercase() && !hasLowercase) {
            errors.add("密码必须包含小写字母");
        } else if (hasLowercase) {
            score.add(20);
        }
    }

    private void validateDigit(String password, List<String> errors, PasswordScore score) {
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        if (passwordPolicyConfig.isRequireDigit() && !hasDigit) {
            errors.add("密码必须包含数字");
        } else if (hasDigit) {
            score.add(20);
        }
    }

    private void validateSpecialChar(String password, List<String> errors, PasswordScore score) {
        if (!passwordPolicyConfig.isRequireSpecialChar()) {
            return;
        }

        boolean hasSpecialChar = containsSpecialChar(password);
        if (!hasSpecialChar) {
            errors.add("密码必须包含特殊字符（" + passwordPolicyConfig.getSpecialChars() + "）");
        } else {
            score.add(20);
        }
    }

    private void addBonusScore(String password, PasswordScore score) {
        if (password.length() >= 12) {
            score.add(10);
        } else if (password.length() >= 10) {
            score.add(5);
        }

        if (hasMultipleCharacterTypes(password)) {
            score.add(10);
        }
    }

    private boolean hasMultipleCharacterTypes(String password) {
        return MULTIPLE_UPPERCASE.matcher(password).find()
                && MULTIPLE_LOWERCASE.matcher(password).find()
                && MULTIPLE_DIGIT.matcher(password).find()
                && hasMultipleSpecialChars(password);
    }

    private boolean hasMultipleSpecialChars(String password) {
        String specialChars = passwordPolicyConfig.getSpecialChars();
        Pattern pattern = Pattern.compile("[" + Pattern.quote(specialChars) + "].*[" + Pattern.quote(specialChars) + "]");
        return pattern.matcher(password).find();
    }

    public List<String> getPasswordImprovementSuggestions(String password) {
        List<String> suggestions = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            suggestions.add("建议使用至少12位的密码");
            return suggestions;
        }

        addLengthSuggestions(password, suggestions);
        addCharacterSuggestions(password, suggestions);

        if (suggestions.isEmpty()) {
            suggestions.add("当前密码强度良好，建议定期更换密码");
        }

        return suggestions;
    }

    private void addLengthSuggestions(String password, List<String> suggestions) {
        int minLen = passwordPolicyConfig.getMinLength();
        if (password.length() < minLen) {
            suggestions.add("增加密码长度至至少" + minLen + "位");
        } else if (password.length() < 12) {
            suggestions.add("增加密码长度至12位以上以提高安全性");
        }
    }

    private void addCharacterSuggestions(String password, List<String> suggestions) {
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            suggestions.add("添加大写字母");
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            suggestions.add("添加小写字母");
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            suggestions.add("添加数字");
        }
        if (!containsSpecialChar(password)) {
            suggestions.add("添加特殊字符（" + passwordPolicyConfig.getSpecialChars() + "）");
        }
    }

    private boolean containsSpecialChar(String password) {
        String specialChars = passwordPolicyConfig.getSpecialChars();
        for (char c : password.toCharArray()) {
            if (specialChars.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static class PasswordScore {
        private int value = 0;

        void add(int points) {
            value += points;
        }

        int getValue() {
            return value;
        }
    }

    public record PasswordValidationResult(boolean isValid, int score, PasswordStrength strength, List<String> errors) {
        public String getStrengthLabel() {
            return strength.getLabel();
        }

        public List<String> getSuggestions() {
            List<String> suggestions = new ArrayList<>();
            if (!isValid) {
                suggestions.addAll(errors);
            }
            return suggestions;
        }
    }
}
