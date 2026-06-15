package com.inventory.common.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for ErrorCodeGenerator.
 */
class ErrorCodeGeneratorTest {

    @BeforeEach
    void setUp() {
        ErrorCodeGenerator.resetAllCounters();
    }

    @AfterEach
    void tearDown() {
        ErrorCodeGenerator.resetAllCounters();
    }

    @Test
    void testGenerateErrorCode() {
        final String errorCode = ErrorCodeGenerator.generateErrorCode("INV", 3);
        assertNotNull(errorCode);
        assertEquals("INV-03-001", errorCode);
    }

    @Test
    void testGenerateErrorCodeSequenceIncrement() {
        final String firstCode = ErrorCodeGenerator.generateErrorCode("INV", 3);
        final String secondCode = ErrorCodeGenerator.generateErrorCode("INV", 3);
        assertEquals("INV-03-001", firstCode);
        assertEquals("INV-03-002", secondCode);
    }

    @Test
    void testGenerateErrorCodeDifferentModules() {
        final String invCode = ErrorCodeGenerator.generateErrorCode("INV", 1);
        final String ordCode = ErrorCodeGenerator.generateErrorCode("ORD", 1);
        assertEquals("INV-01-001", invCode);
        assertEquals("ORD-01-001", ordCode);
    }

    @Test
    void testGenerateErrorCodeFormat() {
        final String errorCode = ErrorCodeGenerator.generateErrorCode("PROD", 2);
        assertTrue(errorCode.matches("^[A-Z]{1,4}-\\d{2}-\\d{3}$"),
                "Error code should match format [ModulePrefix]-[ErrorType]-[Sequence]");
    }

    @Test
    void testGenerateErrorCodeWithStartSequence() {
        final String errorCode = ErrorCodeGenerator.generateErrorCode("SAL", 4, 5);
        assertEquals("SAL-04-005", errorCode);
    }

    @Test
    void testGenerateErrorCodeWithStartSequenceIncrement() {
        ErrorCodeGenerator.generateErrorCode("SAL", 4, 5);
        final String secondCode = ErrorCodeGenerator.generateErrorCode("SAL", 4);
        assertEquals("SAL-04-006", secondCode);
    }

    @Test
    void testGenerateErrorCodeInvalidModulePrefix_Null() {
        assertThrows(IllegalArgumentException.class,
                () -> ErrorCodeGenerator.generateErrorCode(null, 1));
    }

    @Test
    void testGenerateErrorCodeInvalidModulePrefix_Empty() {
        assertThrows(IllegalArgumentException.class,
                () -> ErrorCodeGenerator.generateErrorCode("", 1));
    }

    @Test
    void testGenerateErrorCodeInvalidModulePrefix_TooLong() {
        assertThrows(IllegalArgumentException.class,
                () -> ErrorCodeGenerator.generateErrorCode("TOOLNG", 1));
    }

    @Test
    void testGenerateErrorCodeInvalidModulePrefix_Lowercase() {
        assertThrows(IllegalArgumentException.class,
                () -> ErrorCodeGenerator.generateErrorCode("inv", 1));
    }

    @Test
    void testGenerateErrorCodeInvalidErrorType_TooSmall() {
        assertThrows(IllegalArgumentException.class,
                () -> ErrorCodeGenerator.generateErrorCode("INV", 0));
    }

    @Test
    void testGenerateErrorCodeInvalidErrorType_TooLarge() {
        assertThrows(IllegalArgumentException.class,
                () -> ErrorCodeGenerator.generateErrorCode("INV", 100));
    }

    @Test
    void testResetCounter() {
        ErrorCodeGenerator.generateErrorCode("INV", 3);
        ErrorCodeGenerator.generateErrorCode("INV", 3);
        assertEquals(2, ErrorCodeGenerator.getCurrentCounter("INV", 3));
        ErrorCodeGenerator.resetCounter("INV", 3);
        assertEquals(0, ErrorCodeGenerator.getCurrentCounter("INV", 3));
    }

    @Test
    void testResetAllCounters() {
        ErrorCodeGenerator.generateErrorCode("INV", 1);
        ErrorCodeGenerator.generateErrorCode("ORD", 2);
        ErrorCodeGenerator.resetAllCounters();
        assertEquals(0, ErrorCodeGenerator.getCurrentCounter("INV", 1));
        assertEquals(0, ErrorCodeGenerator.getCurrentCounter("ORD", 2));
    }

    @Test
    void testGetCurrentCounter() {
        assertEquals(0, ErrorCodeGenerator.getCurrentCounter("INV", 1));
        ErrorCodeGenerator.generateErrorCode("INV", 1);
        assertEquals(1, ErrorCodeGenerator.getCurrentCounter("INV", 1));
    }

    @Test
    void testGenerateErrorCodeWithStartSequenceInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> ErrorCodeGenerator.generateErrorCode("INV", 1, 0));
    }

    @Test
    void testGenerateErrorCodeWithStartSequenceInvalidTooLarge() {
        assertThrows(IllegalArgumentException.class,
                () -> ErrorCodeGenerator.generateErrorCode("INV", 1, 1000));
    }
}
