package com.inventory.auth.security.mfa;

import java.security.SecureRandom;

public final class MfaSecretGenerator {    
    private static final int SECRET_SIZE_BYTES = 20;
    private static final SecureRandom RANDOM = new SecureRandom();
    
    private MfaSecretGenerator() {
    }
    
    public static byte[] generateSecret() {
        byte[] secret = new byte[SECRET_SIZE_BYTES];
        RANDOM.nextBytes(secret);
        return secret;
    }
    
    public static String generateSecretBase32() {
        byte[] secret = generateSecret();
        return Base32Encoder.encode(secret);
    }
    
    public static byte[] decodeBase32(String base32) {
        return Base32Encoder.decode(base32);
    }
    
    public static String generateOtpAuthUri(String issuer, String accountName, String secretBase32) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
            urlEncode(issuer),
            urlEncode(accountName),
            secretBase32,
            urlEncode(issuer)
        );
    }
    
    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
