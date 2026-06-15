package com.inventory.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 数据类型验证工具类.
 *
 * <p>提供各种数据类型的验证方法，确保数据符合JSON标准及项目数据规范。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
public final class DataValidationUtils {

    /** 正则表达式：整数（包括正负整数）. */
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");

    /** 正则表达式：浮点数（包括正负浮点数）. */
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    /** 正则表达式：布尔值（true/false，不区分大小写）. */
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(?i)(true|false)$");

    /**
     * 私有构造函数，防止实例化.
     */
    private DataValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 验证对象是否为null.
     *
     * @param obj 要验证的对象
     * @return true如果对象为null，否则false
     */
    public static boolean isNull(final Object obj) {
        return obj == null;
    }

    /**
     * 验证对象是否不为null.
     *
     * @param obj 要验证的对象
     * @return true如果对象不为null，否则false
     */
    public static boolean isNotNull(final Object obj) {
        return !isNull(obj);
    }

    /**
     * 验证对象是否为字符串类型.
     *
     * @param obj 要验证的对象
     * @return true如果对象为字符串类型，否则false
     */
    public static boolean isString(final Object obj) {
        return obj instanceof String;
    }

    /**
     * 验证对象是否为整数类型.
     *
     * @param obj 要验证的对象
     * @return true如果对象为整数类型，否则false
     */
    public static boolean isInteger(final Object obj) {
        if (obj instanceof Integer) {
            return true;
        }
        if (isString(obj)) {
            return INTEGER_PATTERN.matcher((String) obj).matches();
        }
        return false;
    }

    /**
     * 验证对象是否为长整数类型.
     *
     * @param obj 要验证的对象
     * @return true如果对象为长整数类型，否则false
     */
    public static boolean isLong(final Object obj) {
        return obj instanceof Long;
    }

    /**
     * 验证对象是否为浮点数类型.
     *
     * @param obj 要验证的对象
     * @return true如果对象为浮点数类型，否则false
     */
    public static boolean isDouble(final Object obj) {
        if (obj instanceof Double || obj instanceof Float) {
            return true;
        }
        if (isString(obj)) {
            return DOUBLE_PATTERN.matcher((String) obj).matches();
        }
        return false;
    }

    /**
     * 验证对象是否为布尔类型.
     *
     * @param obj 要验证的对象
     * @return true如果对象为布尔类型，否则false
     */
    public static boolean isBoolean(final Object obj) {
        if (obj instanceof Boolean) {
            return true;
        }
        if (isString(obj)) {
            return BOOLEAN_PATTERN.matcher((String) obj).matches();
        }
        return false;
    }

    /**
     * 验证对象是否为数组类型.
     *
     * @param obj 要验证的对象
     * @return true如果对象为数组类型，否则false
     */
    public static boolean isArray(final Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    /**
     * 验证对象是否为集合类型.
     *
     * @param obj 要验证的对象
     * @return true如果对象为集合类型，否则false
     */
    public static boolean isCollection(final Object obj) {
        return obj instanceof Collection;
    }

    /**
     * 验证对象是否为Map类型.
     *
     * @param obj 要验证的对象
     * @return true如果对象为Map类型，否则false
     */
    public static boolean isMap(final Object obj) {
        return obj instanceof Map;
    }

    /**
     * 验证字符串是否为有效的日期格式（ISO-8601）.
     *
     * @param str 要验证的字符串
     * @return true如果字符串为有效的日期格式，否则false
     */
    public static boolean isDate(final String str) {
        return isValidDateTimeFormat(str, () -> LocalDate.parse(str));
    }

    /**
     * 验证字符串是否为有效的日期时间格式（ISO-8601）.
     *
     * @param str 要验证的字符串
     * @return true如果字符串为有效的日期时间格式，否则false
     */
    public static boolean isDateTime(final String str) {
        return isValidDateTimeFormat(str, () -> LocalDateTime.parse(str));
    }

    /**
     * 验证字符串是否为有效的Instant格式.
     *
     * @param str 要验证的字符串
     * @return true如果字符串为有效的Instant格式，否则false
     */
    public static boolean isInstant(final String str) {
        return isValidDateTimeFormat(str, () -> Instant.parse(str));
    }

    /**
     * 验证字符串是否为有效的时间格式（ISO-8601）.
     *
     * @param str 要验证的字符串
     * @return true如果字符串为有效的时间格式，否则false
     */
    public static boolean isTime(final String str) {
        return isValidDateTimeFormat(str, () -> LocalTime.parse(str));
    }

    /**
     * 通用日期时间格式验证方法.
     *
     * @param str 要验证的字符串
     * @param parser 解析操作
     * @return true如果格式有效，否则false
     */
    private static boolean isValidDateTimeFormat(final String str, final Runnable parser) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            parser.run();
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 验证字符串是否为空或仅包含空白字符.
     *
     * @param str 要验证的字符串
     * @return true如果字符串为空或仅包含空白字符，否则false
     */
    public static boolean isBlank(final String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 验证字符串是否不为空且不只包含空白字符.
     *
     * @param str 要验证的字符串
     * @return true如果字符串不为空且不只包含空白字符，否则false
     */
    public static boolean isNotBlank(final String str) {
        return !isBlank(str);
    }

    /**
     * 验证集合是否为空.
     *
     * @param collection 要验证的集合
     * @return true如果集合为空，否则false
     */
    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 验证集合是否不为空.
     *
     * @param collection 要验证的集合
     * @return true如果集合不为空，否则false
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 验证Map是否为空.
     *
     * @param map 要验证的Map
     * @return true如果Map为空，否则false
     */
    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 验证Map是否不为空.
     *
     * @param map 要验证的Map
     * @return true如果Map不为空，否则false
     */
    public static boolean isNotEmpty(final Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 验证数组是否为空.
     *
     * @param array 要验证的数组
     * @return true如果数组为空，否则false
     */
    public static boolean isEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 验证数组是否不为空.
     *
     * @param array 要验证的数组
     * @return true如果数组不为空，否则false
     */
    public static boolean isNotEmpty(final Object[] array) {
        return !isEmpty(array);
    }
}
