package com.inventory.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringUtils Tests")
class StringUtilsTest {

    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {

        @Test
        @DisplayName("Constants should have correct values")
        void constants_HaveCorrectValues() {
            assertEquals("", StringUtils.EMPTY);
            assertEquals(" ", StringUtils.SPACE);
            assertEquals("\t", StringUtils.TAB);
            assertEquals("\n", StringUtils.NEWLINE);
            assertEquals("\r\n", StringUtils.CRLF);
        }

        @Test
        @DisplayName("INDENT constants should have correct length")
        void indentConstants_HaveCorrectLength() {
            assertEquals(4, StringUtils.INDENT_4.length());
            assertEquals(8, StringUtils.INDENT_8.length());
            assertTrue(StringUtils.INDENT_4.startsWith(" "));
            assertTrue(StringUtils.INDENT_8.startsWith(" "));
        }

        @Test
        @DisplayName("Separator constants should have correct lengths")
        void separatorConstants_HaveCorrectLengths() {
            assertEquals(20, StringUtils.SHORT_SEPARATOR.length());
            assertEquals(40, StringUtils.MEDIUM_SEPARATOR.length());
            assertEquals(60, StringUtils.LONG_SEPARATOR.length());
        }

        @Test
        @DisplayName("Equals constants should have correct lengths")
        void equalsConstants_HaveCorrectLengths() {
            assertEquals(20, StringUtils.SHORT_EQUALS.length());
            assertEquals(40, StringUtils.MEDIUM_EQUALS.length());
            assertEquals(60, StringUtils.LONG_EQUALS.length());
        }

        @Test
        @DisplayName("Asterisk constants should have correct lengths")
        void asteriskConstants_HaveCorrectLengths() {
            assertEquals(20, StringUtils.SHORT_ASTERISK.length());
            assertEquals(40, StringUtils.MEDIUM_ASTERISK.length());
            assertEquals(60, StringUtils.LONG_ASTERISK.length());
        }
    }

    @Nested
    @DisplayName("Repeat Tests")
    class RepeatTests {

        @Test
        @DisplayName("repeat should repeat string n times")
        void repeat_ValidInput_ReturnsRepeatedString() {
            assertEquals("aaa", StringUtils.repeat("a", 3));
            assertEquals("-----", StringUtils.repeat("-", 5));
            assertEquals("abcabcabc", StringUtils.repeat("abc", 3));
        }

        @Test
        @DisplayName("repeat with count 0 should return empty string")
        void repeat_ZeroCount_ReturnsEmptyString() {
            assertEquals("", StringUtils.repeat("a", 0));
        }

        @Test
        @DisplayName("repeat with empty string should return empty string")
        void repeat_EmptyString_ReturnsEmpty() {
            assertEquals("", StringUtils.repeat("", 5));
        }

        @Test
        @DisplayName("repeat with single character should work")
        void repeat_SingleChar_Works() {
            assertEquals("x", StringUtils.repeat("x", 1));
            assertEquals("xxxxx", StringUtils.repeat("x", 5));
        }
    }


}
