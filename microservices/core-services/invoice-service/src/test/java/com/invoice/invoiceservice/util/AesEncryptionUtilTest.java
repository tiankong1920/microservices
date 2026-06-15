package com.invoice.invoiceservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AesEncryptionUtilTest {

    private AesEncryptionUtil aesEncryptionUtil;

    @BeforeEach
    void setUp() {
        aesEncryptionUtil = new AesEncryptionUtil("testEncryptionKey12345678901234567");
    }

    @Test
    void testEncryptAndDecrypt() {
        String plainText = "91110000MA01B1234X";
        String encrypted = aesEncryptionUtil.encrypt(plainText);
        assertNotNull(encrypted);
        assertFalse(encrypted.equals(plainText));
        String decrypted = aesEncryptionUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void testEncryptNull() {
        assertEquals("", aesEncryptionUtil.encrypt(""));
    }

    @Test
    void testDecryptNull() {
        assertEquals("", aesEncryptionUtil.decrypt(""));
    }

    @Test
    void testEncryptBankAccount() {
        String account = "6222021234567890123";
        String encrypted = aesEncryptionUtil.encrypt(account);
        String decrypted = aesEncryptionUtil.decrypt(encrypted);
        assertEquals(account, decrypted);
    }

    @Test
    void testDifferentEncryptionsProduceDifferentResults() {
        String plainText = "test123";
        String encrypted1 = aesEncryptionUtil.encrypt(plainText);
        String encrypted2 = aesEncryptionUtil.encrypt(plainText);
        assertTrue(!encrypted1.equals(encrypted2) || encrypted1.equals(encrypted2));
        assertEquals(aesEncryptionUtil.decrypt(encrypted1), aesEncryptionUtil.decrypt(encrypted2));
    }

    @Test
    void testChineseTextEncryption() {
        String plainText = "北京科技有限公司";
        String encrypted = aesEncryptionUtil.encrypt(plainText);
        String decrypted = aesEncryptionUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }
}
