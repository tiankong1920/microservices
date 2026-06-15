package com.inventory.templateservice.util;

import java.util.regex.Pattern;

public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    public static final Pattern EMAIL = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public static final Pattern PHONE = Pattern.compile(
            "^1[3-9]\\d{9}$"
    );

    public static final Pattern ID_CARD = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$"
    );

    public static final Pattern POSTAL_CODE = Pattern.compile(
            "^[1-9]\\d{5}$"
    );

    public static final Pattern URL = Pattern.compile(
            "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
    );

    public static final Pattern IP_ADDRESS = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );

    public static final Pattern DATE = Pattern.compile(
            "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"
    );

    public static final Pattern TIME = Pattern.compile(
            "^([01]?\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$"
    );

    public static final Pattern DATETIME = Pattern.compile(
            "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])\\s([01]?\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$"
    );

    public static final Pattern NUMBER = Pattern.compile(
            "^-?\\d+(\\.\\d+)?$"
    );

    public static final Pattern POSITIVE_INTEGER = Pattern.compile(
            "^[1-9]\\d*$"
    );

    public static final Pattern NON_NEGATIVE_INTEGER = Pattern.compile(
            "^\\d+$"
    );

    public static final Pattern LETTERS_ONLY = Pattern.compile(
            "^[a-zA-Z]+$"
    );

    public static final Pattern ALPHANUMERIC = Pattern.compile(
            "^[a-zA-Z0-9]+$"
    );

    public static final Pattern CHINESE = Pattern.compile(
            "^[\\u4e00-\\u9fa5]+$"
    );

    public static final Pattern TEMPLATE_CODE = Pattern.compile(
            "^TPL_[A-Z]{2,4}_\\d{3,}$"
    );

    public static final Pattern FIELD_CODE = Pattern.compile(
            "^[a-zA-Z][a-zA-Z0-9_]{1,49}$"
    );

    public static final Pattern VERSION = Pattern.compile(
            "^v?(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
    );

    public static boolean matches(Pattern pattern, String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return pattern.matcher(input).matches();
    }
}
