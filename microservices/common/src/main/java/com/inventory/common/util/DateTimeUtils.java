package com.inventory.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * 日期时间工具类.
 *
 * <p>提供标准化的日期时间处理，确保所有日期时间格式符合ISO 8601标准。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
public final class DateTimeUtils {

    /** 默认的日期时间格式：yyyy-MM-dd HH:mm:ss. */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** ISO 8601日期时间格式：yyyy-MM-dd'T'HH:mm:ss.SSSZ. */
    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /** 默认的日期格式：yyyy-MM-dd. */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /** 默认的时间格式：HH:mm:ss. */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /** 默认的日期时间格式化器. */
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);

    /** ISO 8601日期时间格式化器. */
    public static final DateTimeFormatter ISO_8601_FORMATTER =
            DateTimeFormatter.ofPattern(ISO_8601_FORMAT);

    /** 默认的日期格式化器. */
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    /** 默认的时间格式化器. */
    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);

    /**
     * 私有构造函数，防止实例化.
     */
    private DateTimeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 将Instant转换为指定格式的字符串.
     *
     * @param instant Instant对象
     * @param formatter 日期时间格式化器
     * @return 格式化后的日期时间字符串
     */
    public static String formatInstant(final Instant instant, final DateTimeFormatter formatter) {
        if (instant == null || formatter == null) {
            return null;
        }
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return formatter.format(localDateTime);
    }

    /**
     * 将Instant转换为默认格式的字符串（yyyy-MM-dd HH:mm:ss）.
     *
     * @param instant Instant对象
     * @return 格式化后的日期时间字符串
     */
    public static String formatInstant(final Instant instant) {
        return formatInstant(instant, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * 将Instant转换为ISO 8601格式的字符串.
     *
     * @param instant Instant对象
     * @return 格式化后的ISO 8601日期时间字符串
     */
    public static String formatInstantToIso8601(final Instant instant) {
        return formatInstant(instant, ISO_8601_FORMATTER);
    }

    /**
     * 将字符串解析为Instant.
     *
     * @param dateTimeStr 日期时间字符串
     * @param formatter 日期时间格式化器
     * @return Instant对象
     * @throws DateTimeParseException 如果字符串格式不符合要求
     */
    public static Instant parseToInstant(final String dateTimeStr,
            final DateTimeFormatter formatter) throws DateTimeParseException {
        if (dateTimeStr == null || formatter == null) {
            return null;
        }
        final LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * 将默认格式的字符串解析为Instant.
     *
     * @param dateTimeStr 日期时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return Instant对象
     * @throws DateTimeParseException 如果字符串格式不符合要求
     */
    public static Instant parseToInstant(final String dateTimeStr) throws DateTimeParseException {
        return parseToInstant(dateTimeStr, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * 将ISO 8601格式的字符串解析为Instant.
     *
     * @param iso8601Str ISO 8601日期时间字符串
     * @return Instant对象
     * @throws DateTimeParseException 如果字符串格式不符合要求
     */
    public static Instant parseIso8601ToInstant(final String iso8601Str)
            throws DateTimeParseException {
        return parseToInstant(iso8601Str, ISO_8601_FORMATTER);
    }

    /**
     * 将Date转换为Instant.
     *
     * @param date Date对象
     * @return Instant对象
     */
    public static Instant dateToInstant(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant();
    }

    /**
     * 将Instant转换为Date.
     *
     * @param instant Instant对象
     * @return Date对象
     */
    public static Date instantToDate(final Instant instant) {
        if (instant == null) {
            return null;
        }
        return Date.from(instant);
    }

    /**
     * 验证日期时间字符串是否符合指定格式.
     *
     * @param dateTimeStr 日期时间字符串
     * @param formatter 日期时间格式化器
     * @return true如果格式正确，否则false
     */
    public static boolean isValidDateTime(final String dateTimeStr,
            final DateTimeFormatter formatter) {
        try {
            parseToInstant(dateTimeStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 验证日期时间字符串是否符合默认格式（yyyy-MM-dd HH:mm:ss）.
     *
     * @param dateTimeStr 日期时间字符串
     * @return true如果格式正确，否则false
     */
    public static boolean isValidDateTime(final String dateTimeStr) {
        return isValidDateTime(dateTimeStr, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * 验证日期时间字符串是否符合ISO 8601格式.
     *
     * @param iso8601Str ISO 8601日期时间字符串
     * @return true如果格式正确，否则false
     */
    public static boolean isValidIso8601DateTime(final String iso8601Str) {
        return isValidDateTime(iso8601Str, ISO_8601_FORMATTER);
    }

    /**
     * 验证日期字符串是否符合默认格式（yyyy-MM-dd）.
     *
     * @param dateStr 日期字符串
     * @return true如果格式正确，否则false
     */
    public static boolean isValidDate(final String dateStr) {
        return isValidDateTimeFormat(dateStr, () -> DEFAULT_DATE_FORMATTER.parse(dateStr));
    }

    /**
     * 验证时间字符串是否符合默认格式（HH:mm:ss）.
     *
     * @param timeStr 时间字符串
     * @return true如果格式正确，否则false
     */
    public static boolean isValidTime(final String timeStr) {
        return isValidDateTimeFormat(timeStr, () -> DEFAULT_TIME_FORMATTER.parse(timeStr));
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
}
