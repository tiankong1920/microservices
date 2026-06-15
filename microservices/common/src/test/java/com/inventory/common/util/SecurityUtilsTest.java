package com.inventory.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityUtils Tests")
class SecurityUtilsTest {

    @Nested
    @DisplayName("escapeHtml Tests")
    class EscapeHtmlTests {

        @Test
        @DisplayName("escapeHtml should escape ampersand")
        void escapeHtml_Ampersand_Escapes() {
            assertEquals("&amp;", SecurityUtils.escapeHtml("&"));
        }

        @Test
        @DisplayName("escapeHtml should escape less than")
        void escapeHtml_LessThan_Escapes() {
            assertEquals("&lt;", SecurityUtils.escapeHtml("<"));
        }

        @Test
        @DisplayName("escapeHtml should escape greater than")
        void escapeHtml_GreaterThan_Escapes() {
            assertEquals("&gt;", SecurityUtils.escapeHtml(">"));
        }

        @Test
        @DisplayName("escapeHtml should escape double quote")
        void escapeHtml_DoubleQuote_Escapes() {
            assertEquals("&quot;", SecurityUtils.escapeHtml("\""));
        }

        @Test
        @DisplayName("escapeHtml should escape single quote")
        void escapeHtml_SingleQuote_Escapes() {
            assertEquals("&#39;", SecurityUtils.escapeHtml("'"));
        }

        @Test
        @DisplayName("escapeHtml should escape multiple characters")
        void escapeHtml_MultipleChars_EscapesAll() {
            String input = "<script>alert('XSS')</script>";
            String expected = "&lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;";
            assertEquals(expected, SecurityUtils.escapeHtml(input));
        }

        @Test
        @DisplayName("escapeHtml with null should return null")
        void escapeHtml_Null_ReturnsNull() {
            assertNull(SecurityUtils.escapeHtml(null));
        }

        @Test
        @DisplayName("escapeHtml with empty string should return empty")
        void escapeHtml_Empty_ReturnsEmpty() {
            assertEquals("", SecurityUtils.escapeHtml(""));
        }

        @Test
        @DisplayName("escapeHtml with no special chars should return unchanged")
        void escapeHtml_NoSpecialChars_ReturnsUnchanged() {
            assertEquals("Hello World", SecurityUtils.escapeHtml("Hello World"));
        }
    }

    @Nested
    @DisplayName("maskSensitiveData Tests")
    class MaskSensitiveDataTests {

        @Test
        @DisplayName("maskSensitiveData with null should return null string")
        void maskSensitiveData_Null_ReturnsNullString() {
            assertEquals("null", SecurityUtils.maskSensitiveData(null));
        }

        @Test
        @DisplayName("maskSensitiveData with short string should return masked")
        void maskSensitiveData_ShortString_ReturnsMasked() {
            assertEquals("****", SecurityUtils.maskSensitiveData("abc"));
            assertEquals("****", SecurityUtils.maskSensitiveData("ab"));
            assertEquals("****", SecurityUtils.maskSensitiveData("a"));
        }

        @Test
        @DisplayName("maskSensitiveData with exact 4 chars should return masked")
        void maskSensitiveData_Exact4Chars_ReturnsMasked() {
            assertEquals("****", SecurityUtils.maskSensitiveData("abcd"));
        }

        @Test
        @DisplayName("maskSensitiveData with longer string should partially mask")
        void maskSensitiveData_LongString_PartiallyMasked() {
            String result = SecurityUtils.maskSensitiveData("password123");
            assertTrue(result.startsWith("pa"));
            assertTrue(result.endsWith("23"));
            assertTrue(result.contains("****"));
            assertEquals("pa****23", result);
        }

        @Test
        @DisplayName("maskSensitiveData with integer should work")
        void maskSensitiveData_Integer_Works() {
            assertEquals("12****45", SecurityUtils.maskSensitiveData(12345));
        }

        @ParameterizedTest
        @ValueSource(strings = {"creditcard1234", "secretpassword", "token12345678"})
        @DisplayName("maskSensitiveData should mask various sensitive strings")
        void maskSensitiveData_VariousStrings_MasksCorrectly(String input) {
            String result = SecurityUtils.maskSensitiveData(input);
            assertTrue(result.startsWith(input.substring(0, 2)));
            assertTrue(result.endsWith(input.substring(input.length() - 2)));
            assertTrue(result.contains("****"));
        }
    }


}
