package com.inventory.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateTimeUtils Tests")
class DateTimeUtilsTest {

    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {

        @Test
        @DisplayName("Constants should have correct values")
        void constants_HaveCorrectValues() {
            assertEquals("yyyy-MM-dd HH:mm:ss", DateTimeUtils.DEFAULT_DATETIME_FORMAT);
            assertEquals("yyyy-MM-dd'T'HH:mm:ss.SSSZ", DateTimeUtils.ISO_8601_FORMAT);
            assertEquals("yyyy-MM-dd", DateTimeUtils.DEFAULT_DATE_FORMAT);
            assertEquals("HH:mm:ss", DateTimeUtils.DEFAULT_TIME_FORMAT);
        }

        @Test
        @DisplayName("Formatters should not be null")
        void formatters_NotNull() {
            assertNotNull(DateTimeUtils.DEFAULT_DATETIME_FORMATTER);
            assertNotNull(DateTimeUtils.ISO_8601_FORMATTER);
            assertNotNull(DateTimeUtils.DEFAULT_DATE_FORMATTER);
            assertNotNull(DateTimeUtils.DEFAULT_TIME_FORMATTER);
        }
    }

    @Nested
    @DisplayName("formatInstant Tests")
    class FormatInstantTests {

        @Test
        @DisplayName("formatInstant with default formatter should format correctly")
        void formatInstant_DefaultFormatter_FormatsCorrectly() {
            Instant instant = Instant.parse("2024-01-15T10:30:00Z");
            String result = DateTimeUtils.formatInstant(instant);
            assertNotNull(result);
            assertTrue(result.contains("2024"));
        }

        @Test
        @DisplayName("formatInstant with custom formatter should format correctly")
        void formatInstant_CustomFormatter_FormatsCorrectly() {
            Instant instant = Instant.parse("2024-01-15T10:30:00Z");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            String result = DateTimeUtils.formatInstant(instant, formatter);
            assertNotNull(result);
            assertTrue(result.contains("2024/01/15"));
        }

        @Test
        @DisplayName("formatInstant with null instant should return null")
        void formatInstant_NullInstant_ReturnsNull() {
            assertNull(DateTimeUtils.formatInstant(null));
        }

        @Test
        @DisplayName("formatInstant with null formatter should return null")
        void formatInstant_NullFormatter_ReturnsNull() {
            Instant instant = Instant.now();
            assertNull(DateTimeUtils.formatInstant(instant, null));
        }

        @Test
        @DisplayName("formatInstant with both null should return null")
        void formatInstant_BothNull_ReturnsNull() {
            assertNull(DateTimeUtils.formatInstant(null, null));
        }
    }


}
