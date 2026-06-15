package com.inventory.auth.security.mfa;

public final class Base32Encoder {
    
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final int[] DECODE_MAP = new int[128];
    
    static {
        for (int i = 0; i < DECODE_MAP.length; i++) {
            DECODE_MAP[i] = -1;
        }
        for (int i = 0; i < ALPHABET.length; i++) {
            DECODE_MAP[ALPHABET[i]] = i;
            if (ALPHABET[i] >= 'A' && ALPHABET[i] <= 'Z') {
                DECODE_MAP[ALPHABET[i] + 32] = i;
            }
        }
    }
    
    private Base32Encoder() {
    }
    
    public static String encode(byte[] data) {
        StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bufferBits = 0;
        
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xff);
            bufferBits += 8;
            
            while (bufferBits >= 5) {
                bufferBits -= 5;
                int index = (buffer >> bufferBits) & 0x1f;
                result.append(ALPHABET[index]);
            }
        }
        
        if (bufferBits > 0) {
            int index = (buffer << (5 - bufferBits)) & 0x1f;
            result.append(ALPHABET[index]);
        }
        
        return result.toString();
    }
    
    public static byte[] decode(String base32) {
        base32 = base32.replaceAll("[=\\s]", "").toUpperCase();
        
        int buffer = 0;
        int bufferBits = 0;
        int outputLength = base32.length() * 5 / 8;
        byte[] result = new byte[outputLength];
        int resultIndex = 0;
        
        for (char c : base32.toCharArray()) {
            if (c >= 128 || DECODE_MAP[c] == -1) {
                throw new IllegalArgumentException("Invalid Base32 character: " + c);
            }
            
            buffer = (buffer << 5) | DECODE_MAP[c];
            bufferBits += 5;
            
            if (bufferBits >= 8) {
                bufferBits -= 8;
                result[resultIndex++] = (byte) ((buffer >> bufferBits) & 0xff);
            }
        }
        
        return result;
    }
}
