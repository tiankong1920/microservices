package com.inventory.common.util;

/**
 * 字符串工具类.
 *
 * <p>提供字符串处理相关的工具方法，包括重复字符串生成、常量定义等功能。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>生成重复字符串常量</li>
 *   <li>创建格式化的重复字符串</li>
 *   <li>验证字符串重复模式</li>
 * </ul></p>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 生成重复字符串常量
 * public static final String INDENT = StringUtils.repeat(" ", 4);
 * public static final String SEPARATOR = StringUtils.repeat("-", 20);
 *
 * // 使用预定义的重复字符串
 * String paddedText = StringUtils.repeat("=", 50);
 * }</pre></p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 5.0
 */
public final class StringUtils {

    /** 空字符串常量. */
    public static final String EMPTY = "";

    /** 单个空格. */
    public static final String SPACE = " ";

    /** 制表符. */
    public static final String TAB = "\t";

    /** 换行符. */
    public static final String NEWLINE = "\n";

    /** 回车换行符. */
    public static final String CRLF = "\r\n";

    /** 4个空格缩进. */
    public static final String INDENT_4 = repeat(SPACE, 4);

    /** 8个空格缩进. */
    public static final String INDENT_8 = repeat(SPACE, 8);

    /** 短分隔线（20个减号）. */
    public static final String SHORT_SEPARATOR = repeat("-", 20);

    /** 中等分隔线（40个减号）. */
    public static final String MEDIUM_SEPARATOR = repeat("-", 40);

    /** 长分隔线（60个减号）. */
    public static final String LONG_SEPARATOR = repeat("-", 60);

    /** 短等号分隔线（20个等号）. */
    public static final String SHORT_EQUALS = repeat("=", 20);

    /** 中等等号分隔线（40个等号）. */
    public static final String MEDIUM_EQUALS = repeat("=", 40);

    /** 长等号分隔线（60个等号）. */
    public static final String LONG_EQUALS = repeat("=", 60);

    /** 短星号分隔线（20个星号）. */
    public static final String SHORT_ASTERISK = repeat("*", 20);

    /** 中等星号分隔线（40个星号）. */
    public static final String MEDIUM_ASTERISK = repeat("*", 40);

    /** 长星号分隔线（60个星号）. */
    public static final String LONG_ASTERISK = repeat("*", 60);

    /** 最大重复次数限制，防止内存溢出. */
    private static final int MAX_REPEAT_COUNT = 10000;

    /**
     * 私有构造函数，防止实例化.
     */
    private StringUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 生成重复字符串.
     *
     * <p>将指定字符串重复指定次数，生成新的字符串。
     * 如果重复次数为0或负数，返回空字符串。</p>
     *
     * <p>使用示例：
     * <pre>{@code
     * String separator = StringUtils.repeat("-", 10); // "----------"
     * String padding = StringUtils.repeat(" ", 4);    // "    "
     * String pattern = StringUtils.repeat("ab", 3);   // "ababab"
     * }</pre></p>
     *
     * @param str 要重复的基础字符串，如果为null则返回空字符串
     * @param count 重复次数，如果小于等于0则返回空字符串
     * @return 重复后的字符串
     * @throws IllegalArgumentException 如果重复次数超过最大限制（10000）
     */
    public static String repeat(final String str, final int count) {
        if (str == null || str.isEmpty()) {
            return EMPTY;
        }
        if (count <= 0) {
            return EMPTY;
        }
        if (count > MAX_REPEAT_COUNT) {
            throw new IllegalArgumentException(
                    "Repeat count exceeds maximum limit: " + MAX_REPEAT_COUNT);
        }
        if (count == 1) {
            return str;
        }

        final StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 生成重复字符串，并用分隔符连接.
     *
     * <p>将指定字符串重复指定次数，每次重复之间用分隔符连接。</p>
     *
     * <p>使用示例：
     * <pre>{@code
     * String csv = StringUtils.repeatWithSeparator("?", 3, ","); // "?,?,?"
     * String list = StringUtils.repeatWithSeparator("item", 3, " | "); // "item | item | item"
     * }</pre></p>
     *
     * @param str 要重复的基础字符串
     * @param count 重复次数
     * @param separator 分隔符
     * @return 重复并用分隔符连接后的字符串
     */
    public static String repeatWithSeparator(final String str, final int count,
            final String separator) {
        if (str == null || str.isEmpty() || count <= 0) {
            return EMPTY;
        }
        if (count == 1) {
            return str;
        }
        if (separator == null || separator.isEmpty()) {
            return repeat(str, count);
        }

        final StringBuilder sb = new StringBuilder(
                (str.length() + separator.length()) * count);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 生成居中的重复字符串.
     *
     * <p>生成指定长度的重复字符串，并将目标字符串居中放置。</p>
     *
     * <p>使用示例：
     * <pre>{@code
     * String centered = StringUtils.center("Hello", 10, "-"); // "--Hello---"
     * }</pre></p>
     *
     * @param str 要居中的字符串
     * @param width 总宽度
     * @param padStr 填充字符串
     * @return 居中后的字符串
     */
    public static String center(final String str, final int width, final String padStr) {
        if (str == null || width <= str.length()) {
            return str != null ? str : EMPTY;
        }
        if (padStr == null || padStr.isEmpty()) {
            return str;
        }

        final int padLen = width - str.length();
        final int padStart = padLen / 2;
        final int padEnd = padLen - padStart;

        final StringBuilder sb = new StringBuilder(width);
        sb.append(repeat(padStr, (padStart + padStr.length() - 1) / padStr.length()));
        sb.append(str);
        sb.append(repeat(padStr, (padEnd + padStr.length() - 1) / padStr.length()));

        return sb.length() > width ? sb.substring(0, width) : sb.toString();
    }

    /**
     * 生成左对齐的重复字符串.
     *
     * <p>生成指定长度的字符串，目标字符串左对齐，右侧用填充字符串填充。</p>
     *
     * @param str 要左对齐的字符串
     * @param width 总宽度
     * @param padStr 填充字符串
     * @return 左对齐后的字符串
     */
    public static String leftPad(final String str, final int width, final String padStr) {
        if (str == null) {
            return repeat(padStr, width);
        }
        if (width <= str.length()) {
            return str;
        }
        if (padStr == null || padStr.isEmpty()) {
            return str;
        }

        final int padLen = width - str.length();
        final StringBuilder sb = new StringBuilder(width);
        sb.append(repeat(padStr, (padLen + padStr.length() - 1) / padStr.length()));
        sb.append(str);

        return sb.length() > width ? sb.substring(sb.length() - width) : sb.toString();
    }

    /**
     * 生成右对齐的重复字符串.
     *
     * <p>生成指定长度的字符串，目标字符串右对齐，左侧用填充字符串填充。</p>
     *
     * @param str 要右对齐的字符串
     * @param width 总宽度
     * @param padStr 填充字符串
     * @return 右对齐后的字符串
     */
    public static String rightPad(final String str, final int width, final String padStr) {
        if (str == null) {
            return repeat(padStr, width);
        }
        if (width <= str.length()) {
            return str;
        }
        if (padStr == null || padStr.isEmpty()) {
            return str;
        }

        final int padLen = width - str.length();
        final StringBuilder sb = new StringBuilder(width);
        sb.append(str);
        sb.append(repeat(padStr, (padLen + padStr.length() - 1) / padStr.length()));

        return sb.length() > width ? sb.substring(0, width) : sb.toString();
    }

    /**
     * 创建重复字符串常量的构建器.
     *
     * <p>提供流式API来构建重复字符串常量。</p>
     *
     * <p>使用示例：
     * <pre>{@code
     * String constant = StringUtils.builder()
     *     .base("abc")
     *     .repeat(5)
     *     .build();
     * }</pre></p>
     *
     * @return 构建器实例
     */
    public static RepeatStringBuilder builder() {
        return new RepeatStringBuilder();
    }

    /**
     * 重复字符串构建器.
     *
     * <p>提供流式API来构建重复字符串常量，便于在代码中定义可读性高的常量。</p>
     */
    public static final class RepeatStringBuilder {

        /** 基础字符串. */
        private String baseString = EMPTY;

        /** 重复次数. */
        private int repeatCount = 1;

        /** 分隔符. */
        private String separator = EMPTY;

        /**
         * 私有构造函数.
         */
        private RepeatStringBuilder() {
        }

        /**
         * 设置基础字符串.
         *
         * @param base 基础字符串
         * @return 构建器实例
         */
        public RepeatStringBuilder base(final String base) {
            this.baseString = base != null ? base : EMPTY;
            return this;
        }

        /**
         * 设置重复次数.
         *
         * @param count 重复次数
         * @return 构建器实例
         */
        public RepeatStringBuilder repeat(final int count) {
            this.repeatCount = count;
            return this;
        }

        /**
         * 设置分隔符.
         *
         * @param sep 分隔符
         * @return 构建器实例
         */
        public RepeatStringBuilder separator(final String sep) {
            this.separator = sep != null ? sep : EMPTY;
            return this;
        }

        /**
         * 构建重复字符串.
         *
         * @return 生成的重复字符串
         */
        public String build() {
            if (separator.isEmpty()) {
                return StringUtils.repeat(baseString, repeatCount);
            }
            return repeatWithSeparator(baseString, repeatCount, separator);
        }
    }
}
