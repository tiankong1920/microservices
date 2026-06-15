package com.inventory.auth.security.mfa;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class HmacUtils {
    
    private HmacUtils() {
    }
    
    public static byte[] hmacSha1(byte[] key, byte[] data) {
        return hmac("HmacSHA1", key, data);
    }
    
    public static byte[] hmacSha256(byte[] key, byte[] data) {
        return hmac("HmacSHA256", key, data);
    }
    
    public static byte[] hmac(String algorithm, byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm);
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC: " + algorithm, e);
        }
    }
}
