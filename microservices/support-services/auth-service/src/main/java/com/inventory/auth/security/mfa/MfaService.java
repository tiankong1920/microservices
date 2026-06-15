package com.inventory.auth.security.mfa;

import org.springframework.stereotype.Service;

@Service
public class MfaService {
    
    private static final int DEFAULT_WINDOW_SIZE = 1;
    
    public MfaSetupResponse setupMfa(String username) {
        String secret = MfaSecretGenerator.generateSecretBase32();
        String otpAuthUri = MfaSecretGenerator.generateOtpAuthUri(
            "InventorySystem",
            username,
            secret
        );
        String qrCodeUri = generateQrCodeUri(otpAuthUri);
        
        return new MfaSetupResponse(secret, otpAuthUri, qrCodeUri);
    }
    
    public boolean verifyTotp(String secretBase32, String totp) {
        if (secretBase32 == null || totp == null || totp.length() != 6) {
            return false;
        }
        
        try {
            byte[] secret = MfaSecretGenerator.decodeBase32(secretBase32);
            return TotpGenerator.validateTotp(secret, totp, DEFAULT_WINDOW_SIZE);
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean verifyTotpWithSecret(byte[] secret, String totp) {
        if (secret == null || totp == null || totp.length() != 6) {
            return false;
        }
        
        try {
            return TotpGenerator.validateTotp(secret, totp, DEFAULT_WINDOW_SIZE);
        } catch (Exception e) {
            return false;
        }
    }
    
    public long getRemainingSeconds() {
        return TotpGenerator.getRemainingSeconds();
    }
    
    private String generateQrCodeUri(String otpAuthUri) {
        return "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=" 
            + java.net.URLEncoder.encode(otpAuthUri, java.nio.charset.StandardCharsets.UTF_8)
            + "&choe=UTF-8";
    }
    
    public static record MfaSetupResponse(
        String secret,
        String otpAuthUri,
        String qrCodeUri
    ) {}
}
