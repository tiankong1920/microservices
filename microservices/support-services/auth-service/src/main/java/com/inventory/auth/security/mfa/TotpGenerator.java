package com.inventory.auth.security.mfa;

import java.util.concurrent.TimeUnit;

public final class TotpGenerator {
    
    private static final int DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    
    private static final int[] DIGITS_POWER = {
        1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000
    };
    
    private TotpGenerator() {
    }
    
    public static String generateTotp(byte[] secret) {
        return generateTotp(secret, getCurrentTimeStep());
    }
    
    public static String generateTotp(byte[] secret, long timeStep) {
        byte[] counter = longToBytes(timeStep);
        byte[] hash = HmacUtils.hmacSha1(secret, counter);
        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
        int otp = binary % DIGITS_POWER[DIGITS];
        return String.format("%0" + DIGITS + "d", otp);
    }
    
    public static boolean validateTotp(byte[] secret, String totp, int windowSize) {
        long currentTimeStep = getCurrentTimeStep();
        for (int i = -windowSize; i <= windowSize; i++) {
            String expectedTotp = generateTotp(secret, currentTimeStep + i);
            if (expectedTotp.equals(totp)) {
                return true;
            }
        }
        return false;
    }
    
    public static long getCurrentTimeStep() {
        return System.currentTimeMillis() / TimeUnit.SECONDS.toMillis(TIME_STEP_SECONDS);
    }
    
    public static long getRemainingSeconds() {
        return TIME_STEP_SECONDS - (System.currentTimeMillis() / 1000) % TIME_STEP_SECONDS;
    }
    
    private static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xff);
            value >>= 8;
        }
        return result;
    }
}
