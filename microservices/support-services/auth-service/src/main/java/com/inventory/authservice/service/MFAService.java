package com.inventory.authservice.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import com.inventory.authservice.entity.User;
import com.inventory.authservice.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class MFAService {

    private final GoogleAuthenticator gAuth;
    private final IUserRepository userRepository;

    @Autowired
    public MFAService(IUserRepository userRepository) {
        this.gAuth = new GoogleAuthenticator();
        this.userRepository = userRepository;
    }

    /**
 * 为用户生成MFA密钥.
 * @param user 用户对象
 * @return MFA密钥
     */
    public String generateMFASecret(User user) {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();
        user.setMfaSecret(secret);
        user.setMfaRecoveryCode(generateRecoveryCode());
        userRepository.save(user);
        return secret;
    }

    /**
 * 生成MFA二维码URL.
 * @param user 用户对象
 * @param secret MFA密钥
 * @return 二维码URL
     */
    public String generateQRCodeURL(User user, String secret) {
        String issuer = "Inventory Management System";
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secret).build();
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, user.getEmail(), key);
    }

    /**
 * 验证MFA代码.
 * @param user 用户对象
 * @param code MFA代码
 * @return 是否验证成功
     */
    public boolean verifyMFACode(User user, int code) {
        return gAuth.authorize(user.getMfaSecret(), code);
    }

    /**
 * 启用MFA.
 * @param user 用户对象
 * @param code MFA代码
 * @return 是否启用成功
     */
    public boolean enableMFA(User user, int code) {
        if (verifyMFACode(user, code)) {
            user.setMfaEnabled(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
 * 禁用MFA.
 * @param user 用户对象
     */
    public void disableMFA(User user) {
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setMfaRecoveryCode(null);
        userRepository.save(user);
    }

    /**
 * 使用恢复代码禁用MFA.
 * @param user 用户对象
 * @param recoveryCode 恢复代码
 * @return 是否成功
     */
    public boolean disableMFAWithRecoveryCode(User user, String recoveryCode) {
        if (recoveryCode.equals(user.getMfaRecoveryCode())) {
            disableMFA(user);
            return true;
        }
        return false;
    }

    /**
 * 生成恢复代码.
 * @return 恢复代码
     */
    private String generateRecoveryCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 重新生成恢复代码.
     * @param user 用户对象
     * @return 新的恢复代码
     */
    public String regenerateRecoveryCode(User user) {
        String newRecoveryCode = generateRecoveryCode();
        user.setMfaRecoveryCode(newRecoveryCode);
        userRepository.save(user);
        return newRecoveryCode;
    }

    /**
     * 创建MFA会话ID.
     * @param username 用户名
     * @return MFA会话ID
     */
    public String createMfaSession(String username) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 验证MFA会话.
     * @param sessionId 会话ID
     * @param username 用户名
     * @return 是否有效
     */
    public boolean validateMfaSession(String sessionId, String username) {
        return sessionId != null && !sessionId.isEmpty();
    }
}
