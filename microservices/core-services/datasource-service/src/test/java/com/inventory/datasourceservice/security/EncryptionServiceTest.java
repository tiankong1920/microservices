package com.inventory.datasourceservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EncryptionService Unit Tests")
class EncryptionServiceTest {

    private EncryptionService encryptionService;

    private static final String TEST_SECRET_KEY = "test-secret-key-32-bytes-long!!!";

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        ReflectionTestUtils.setField(encryptionService, "secretKey", TEST_SECRET_KEY);
    }

    @Nested
    @DisplayName("encrypt() method tests")
    class EncryptTests {

        @Test
        @DisplayName("should encrypt plain text successfully")
        void shouldEncryptPlainTextSuccessfully() {
            String plainText = "mySecretPassword123!";

            String encrypted = encryptionService.encrypt(plainText);

            assertNotNull(encrypted);
            assertNotEquals(plainText, encrypted);
            assertTrue(encrypted.length() > 0);
        }

        @Test
        @DisplayName("should return null when input is null")
        void shouldReturnNullWhenInputIsNull() {
            String result = encryptionService.encrypt(null);

            assertNull(result);
        }

        @Test
        @DisplayName("should return empty string when input is empty")
        void shouldReturnEmptyStringWhenInputIsEmpty() {
            String result = encryptionService.encrypt("");

            assertEquals("", result);
        }

        @Test
        @DisplayName("should produce different ciphertext for same plaintext")
        void shouldProduceDifferentCiphertextForSamePlaintext() {
            String plainText = "samePassword";

            String encrypted1 = encryptionService.encrypt(plainText);
            String encrypted2 = encryptionService.encrypt(plainText);

            assertNotEquals(encrypted1, encrypted2, 
                "Each encryption should produce different ciphertext due to random IV");
        }

        @Test
        @DisplayName("should encrypt Chinese characters correctly")
        void shouldEncryptChineseCharactersCorrectly() {
            String plainText = "中文密码测试123";

            String encrypted = encryptionService.encrypt(plainText);
            String decrypted = encryptionService.decrypt(encrypted);

            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("should encrypt special characters correctly")
        void shouldEncryptSpecialCharactersCorrectly() {
            String plainText = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

            String encrypted = encryptionService.encrypt(plainText);
            String decrypted = encryptionService.decrypt(encrypted);

            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("should encrypt long text successfully")
        void shouldEncryptLongTextSuccessfully() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("a");
            }
            String plainText = sb.toString();

            String encrypted = encryptionService.encrypt(plainText);
            String decrypted = encryptionService.decrypt(encrypted);

            assertEquals(plainText, decrypted);
        }
    }

    @Nested
    @DisplayName("decrypt() method tests")
    class DecryptTests {

        @Test
        @DisplayName("should decrypt encrypted text successfully")
        void shouldDecryptEncryptedTextSuccessfully() {
            String plainText = "mySecretPassword";

            String encrypted = encryptionService.encrypt(plainText);
            String decrypted = encryptionService.decrypt(encrypted);

            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("should return null when input is null")
        void shouldReturnNullWhenInputIsNull() {
            String result = encryptionService.decrypt(null);

            assertNull(result);
        }

        @Test
        @DisplayName("should return empty string when input is empty")
        void shouldReturnEmptyStringWhenInputIsEmpty() {
            String result = encryptionService.decrypt("");

            assertEquals("", result);
        }

        @Test
        @DisplayName("should throw exception for invalid base64 string")
        void shouldThrowExceptionForInvalidBase64String() {
            String invalidEncrypted = "not-valid-base64!!!";

            assertThrows(RuntimeException.class, () -> 
                encryptionService.decrypt(invalidEncrypted)
            );
        }

        @Test
        @DisplayName("should throw exception for tampered ciphertext")
        void shouldThrowExceptionForTamperedCiphertext() {
            String plainText = "originalPassword";
            String encrypted = encryptionService.encrypt(plainText);
            String tampered = encrypted.substring(0, encrypted.length() - 5) + "XXXXX";

            assertThrows(RuntimeException.class, () -> 
                encryptionService.decrypt(tampered)
            );
        }

        @Test
        @DisplayName("should throw exception for too short ciphertext")
        void shouldThrowExceptionForTooShortCiphertext() {
            String tooShort = "YWJjZA==";

            assertThrows(RuntimeException.class, () -> 
                encryptionService.decrypt(tooShort)
            );
        }
    }

    @Nested
    @DisplayName("validateEncryptedPassword() method tests")
    class ValidateEncryptedPasswordTests {

        @Test
        @DisplayName("should return true for valid encrypted password")
        void shouldReturnTrueForValidEncryptedPassword() {
            String plainText = "password123";
            String encrypted = encryptionService.encrypt(plainText);

            boolean isValid = encryptionService.validateEncryptedPassword(encrypted);

            assertTrue(isValid);
        }

        @Test
        @DisplayName("should return false for null input")
        void shouldReturnFalseForNullInput() {
            boolean isValid = encryptionService.validateEncryptedPassword(null);

            assertFalse(isValid);
        }

        @Test
        @DisplayName("should return false for empty string")
        void shouldReturnFalseForEmptyString() {
            boolean isValid = encryptionService.validateEncryptedPassword("");

            assertFalse(isValid);
        }

        @Test
        @DisplayName("should return false for invalid base64 string")
        void shouldReturnFalseForInvalidBase64String() {
            boolean isValid = encryptionService.validateEncryptedPassword("not-valid-base64!!!");

            assertFalse(isValid);
        }

        @Test
        @DisplayName("should return false for too short encrypted data")
        void shouldReturnFalseForTooShortEncryptedData() {
            boolean isValid = encryptionService.validateEncryptedPassword("YWJjZA==");

            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Round-trip encryption tests")
    class RoundTripTests {

        @Test
        @DisplayName("should maintain data integrity through multiple encrypt-decrypt cycles")
        void shouldMaintainDataIntegrityThroughMultipleCycles() {
            String original = "testPassword123!@#";

            for (int i = 0; i < 10; i++) {
                String encrypted = encryptionService.encrypt(original);
                String decrypted = encryptionService.decrypt(encrypted);
                assertEquals(original, decrypted, "Failed at cycle " + i);
            }
        }

        @Test
        @DisplayName("should handle various password formats")
        void shouldHandleVariousPasswordFormats() {
            String[] testPasswords = {
                "simple",
                "with spaces",
                "with\nnewline",
                "with\ttab",
                "12345678",
                "!@#$%^&*()",
                "UPPERCASE",
                "MixedCase123",
                "中文密码",
                "日本語パスワード",
                "한국어비밀번호",
                "emoji🔐password",
                "  leading_spaces",
                "trailing_spaces  ",
                "a",
                "a".repeat(1000)
            };

            for (String password : testPasswords) {
                String encrypted = encryptionService.encrypt(password);
                String decrypted = encryptionService.decrypt(encrypted);
                assertEquals(password, decrypted, "Failed for password: " + password);
            }
        }
    }
}
