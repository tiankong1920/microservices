package com.inventory.common.util;

/**
 * 安全工具类.
 *
 * <p>提供HTML转义和数据脱敏功能。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
public final class SecurityUtils {

    /**
     * 私有构造函数，防止实例化.
     */
    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 对HTML字符串进行转义，防止XSS攻击.
     *
     * @param html 要转义的HTML字符串
     * @return 转义后的安全字符串
     */
    public static String escapeHtml(final String html) {
        if (html == null) {
            return null;
        }
        return html.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * 对敏感数据进行脱敏处理.
     *
     * @param value 要脱敏的值
     * @return 脱敏后的字符串
     */
    public static String maskSensitiveData(final Object value) {
        if (value == null) {
            return "null";
        }
        final String str = value.toString();
        if (str.length() <= 4) {
            return "****";
        }
        return str.substring(0, 2) + "****" + str.substring(str.length() - 2);
    }
}
