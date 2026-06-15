package com.inventory.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DataValidationUtils Tests")
class DataValidationUtilsTest {

    @Nested
    @DisplayName("isNull / isNotNull Tests")
    class NullChecks {

        @Test
        @DisplayName("isNull should return true for null")
        void isNull_Null_ReturnsTrue() {
            assertTrue(DataValidationUtils.isNull(null));
        }

        @Test
        @DisplayName("isNull should return false for non-null")
        void isNull_NonNull_ReturnsFalse() {
            assertFalse(DataValidationUtils.isNull(""));
            assertFalse(DataValidationUtils.isNull(0));
            assertFalse(DataValidationUtils.isNull(new Object()));
        }

        @Test
        @DisplayName("isNotNull should return true for non-null")
        void isNotNull_NonNull_ReturnsTrue() {
            assertTrue(DataValidationUtils.isNotNull(""));
            assertTrue(DataValidationUtils.isNotNull(0));
        }

        @Test
        @DisplayName("isNotNull should return false for null")
        void isNotNull_Null_ReturnsFalse() {
            assertFalse(DataValidationUtils.isNotNull(null));
        }
    }

    @Nested
    @DisplayName("isString Tests")
    class StringTypeTests {

        @Test
        @DisplayName("isString should return true for String")
        void isString_String_ReturnsTrue() {
            assertTrue(DataValidationUtils.isString(""));
            assertTrue(DataValidationUtils.isString("test"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "abc", ""})
        @DisplayName("isString should return true for various strings")
        void isString_VariousStrings_ReturnsTrue(String input) {
            assertTrue(DataValidationUtils.isString(input));
        }

        @Test
        @DisplayName("isString should return false for non-String")
        void isString_NonString_ReturnsFalse() {
            assertFalse(DataValidationUtils.isString(123));
            assertFalse(DataValidationUtils.isString(new Object()));
            assertFalse(DataValidationUtils.isString(List.of()));
            assertFalse(DataValidationUtils.isString(Map.of()));
        }
    }

    @Nested
    @DisplayName("isInteger Tests")
    class IntegerTypeTests {

        @Test
        @DisplayName("isInteger should return true for Integer")
        void isInteger_Integer_ReturnsTrue() {
            assertTrue(DataValidationUtils.isInteger(Integer.valueOf(0)));
            assertTrue(DataValidationUtils.isInteger(Integer.valueOf(-100)));
            assertTrue(DataValidationUtils.isInteger(Integer.valueOf(100)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "123", "-456", "7890"})
        @DisplayName("isInteger should return true for valid integer strings")
        void isInteger_ValidString_ReturnsTrue(String input) {
            assertTrue(DataValidationUtils.isInteger(input));
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "12.34", "true", ""})
        @DisplayName("isInteger should return false for invalid strings")
        void isInteger_InvalidString_ReturnsFalse(String input) {
            assertFalse(DataValidationUtils.isInteger(input));
        }

        @Test
        @DisplayName("isInteger should return false for non-integer types")
        void isInteger_NonInteger_ReturnsFalse() {
            assertFalse(DataValidationUtils.isInteger(Long.valueOf(1)));
            assertFalse(DataValidationUtils.isInteger(1.0));
            assertFalse(DataValidationUtils.isInteger("1.0"));
        }
    }

    @Nested
    @DisplayName("isLong Tests")
    class LongTypeTests {

        @Test
        @DisplayName("isLong should return true for Long")
        void isLong_Long_ReturnsTrue() {
            assertTrue(DataValidationUtils.isLong(Long.valueOf(0)));
            assertTrue(DataValidationUtils.isLong(Long.valueOf(-100)));
            assertTrue(DataValidationUtils.isLong(Long.valueOf(100)));
        }

        @Test
        @DisplayName("isLong should return false for non-Long")
        void isLong_NonLong_ReturnsFalse() {
            assertFalse(DataValidationUtils.isLong(Integer.valueOf(1)));
            assertFalse(DataValidationUtils.isLong("1"));
            assertFalse(DataValidationUtils.isLong(1.0));
        }
    }

    @Nested
    @DisplayName("isDouble Tests")
    class DoubleTypeTests {

        @Test
        @DisplayName("isDouble should return true for Double")
        void isDouble_Double_ReturnsTrue() {
            assertTrue(DataValidationUtils.isDouble(Double.valueOf(0.0)));
            assertTrue(DataValidationUtils.isDouble(Double.valueOf(-1.5)));
            assertTrue(DataValidationUtils.isDouble(Double.valueOf(3.14159)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.0", "1.5", "-3.14", "100.0"})
        @DisplayName("isDouble should return true for valid double strings")
        void isDouble_ValidString_ReturnsTrue(String input) {
            assertTrue(DataValidationUtils.isDouble(input));
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "1.", "true"})
        @DisplayName("isDouble should return false for invalid strings")
        void isDouble_InvalidString_ReturnsFalse(String input) {
            assertFalse(DataValidationUtils.isDouble(input));
        }
    }

    @Nested
    @DisplayName("isBoolean Tests")
    class BooleanTypeTests {

        @Test
        @DisplayName("isBoolean should return true for Boolean")
        void isBoolean_Boolean_ReturnsTrue() {
            assertTrue(DataValidationUtils.isBoolean(Boolean.TRUE));
            assertTrue(DataValidationUtils.isBoolean(Boolean.FALSE));
        }

        @ParameterizedTest
        @ValueSource(strings = {"true", "false", "TRUE", "FALSE", "True", "False"})
        @DisplayName("isBoolean should return true for boolean strings (case insensitive)")
        void isBoolean_ValidString_ReturnsTrue(String input) {
            assertTrue(DataValidationUtils.isBoolean(input));
        }

        @ParameterizedTest
        @ValueSource(strings = {"1", "0", "yes", "no", ""})
        @DisplayName("isBoolean should return false for non-boolean strings")
        void isBoolean_InvalidString_ReturnsFalse(String input) {
            assertFalse(DataValidationUtils.isBoolean(input));
        }
    }

    @Nested
    @DisplayName("isCollection Tests")
    class CollectionTypeTests {

        @Test
        @DisplayName("isCollection should return true for List")
        void isCollection_List_ReturnsTrue() {
            assertTrue(DataValidationUtils.isCollection(List.of()));
            assertTrue(DataValidationUtils.isCollection(List.of(1, 2, 3)));
        }

        @Test
        @DisplayName("isCollection should return true for Set")
        void isCollection_Set_ReturnsTrue() {
            assertTrue(DataValidationUtils.isCollection(java.util.Set.of()));
            assertTrue(DataValidationUtils.isCollection(java.util.Set.of(1, 2, 3)));
        }

        @Test
        @DisplayName("isCollection should return false for non-collections")
        void isCollection_NonCollection_ReturnsFalse() {
            assertFalse(DataValidationUtils.isCollection("string"));
            assertFalse(DataValidationUtils.isCollection(123));
            assertFalse(DataValidationUtils.isCollection(Map.of()));
            assertFalse(DataValidationUtils.isCollection(new int[0]));
            assertFalse(DataValidationUtils.isCollection(new String[]{"a", "b"}));
        }
    }

    @Nested
    @DisplayName("isMap Tests")
    class MapTypeTests {

        @Test
        @DisplayName("isMap should return true for Map")
        void isMap_Map_ReturnsTrue() {
            assertTrue(DataValidationUtils.isMap(Map.of()));
            assertTrue(DataValidationUtils.isMap(Map.of("key", "value")));
        }

        @Test
        @DisplayName("isMap should return false for non-Map")
        void isMap_NonMap_ReturnsFalse() {
            assertFalse(DataValidationUtils.isMap(List.of()));
            assertFalse(DataValidationUtils.isMap("string"));
            assertFalse(DataValidationUtils.isMap(123));
        }
    }

    @Nested
    @DisplayName("isEmpty / isNotEmpty Tests")
    class EmptyTests {

        @Test
        @DisplayName("isEmpty should return true for empty collection")
        void isEmpty_EmptyCollection_ReturnsTrue() {
            assertTrue(DataValidationUtils.isEmpty(List.of()));
        }

        @Test
        @DisplayName("isEmpty should return true for empty map")
        void isEmpty_EmptyMap_ReturnsTrue() {
            assertTrue(DataValidationUtils.isEmpty(Map.of()));
        }

        @Test
        @DisplayName("isEmpty should return true for empty array")
        void isEmpty_EmptyArray_ReturnsTrue() {
            assertTrue(DataValidationUtils.isEmpty(new String[0]));
        }

        @Test
        @DisplayName("isNotEmpty should return true for non-empty collection")
        void isNotEmpty_NonEmptyCollection_ReturnsTrue() {
            assertTrue(DataValidationUtils.isNotEmpty(List.of(1)));
            assertTrue(DataValidationUtils.isNotEmpty(List.of("a", "b")));
        }

        @Test
        @DisplayName("isNotEmpty should return true for non-empty map")
        void isNotEmpty_NonEmptyMap_ReturnsTrue() {
            assertTrue(DataValidationUtils.isNotEmpty(Map.of("k", "v")));
        }
    }


}
