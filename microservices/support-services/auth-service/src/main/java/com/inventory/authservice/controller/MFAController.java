package com.inventory.authservice.controller;

import com.inventory.authservice.entity.User;
import com.inventory.authservice.repository.IUserRepository;
import com.inventory.authservice.service.MFAService;
import com.inventory.common.core.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * MFA控制器 - 处理多因素认证的初始化、启用和禁用
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mfa")
@Validated
public class MFAController {

    private final MFAService mfaService;
    private final IUserRepository userRepository;

    @Autowired
    public MFAController(MFAService mfaService, IUserRepository userRepository) {
        this.mfaService = mfaService;
        this.userRepository = userRepository;
    }

    /**
     * 初始化MFA
     *
     * @param userDetails 当前登录用户信息
     * @return 包含MFA密钥和二维码URL的响应
     */
    @PostMapping("/init")
    public ResponseEntity<?> initMFA(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

        String secret = mfaService.generateMFASecret(user);
        String qrCodeUrl = mfaService.generateQRCodeURL(user, secret);

        Map<String, Object> response = new HashMap<>();
        response.put(MessageConstants.KEY_SECRET, secret);
        response.put(MessageConstants.KEY_QR_CODE_URL, qrCodeUrl);
        response.put(MessageConstants.KEY_RECOVERY_CODE, user.getMfaRecoveryCode());

        return ResponseEntity.ok(response);
    }

    /**
     * 启用MFA
     *
     * @param userDetails 当前登录用户信息
     * @param code 用户输入的MFA验证码
     * @return 启用成功或失败响应
     */
    @PostMapping("/enable")
    public ResponseEntity<?> enableMFA(@AuthenticationPrincipal UserDetails userDetails, @RequestParam int code) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

        boolean enabled = mfaService.enableMFA(user, code);
        if (enabled) {
            return ResponseEntity.ok(Map.of(MessageConstants.KEY_MESSAGE, "MFA enabled successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(MessageConstants.KEY_MESSAGE, MessageConstants.INVALID_MFA_CODE));
        }
    }

    /**
     * 禁用MFA
     *
     * @param userDetails 当前登录用户信息
     * @return 禁用成功响应
     */
    @PostMapping("/disable")
    public ResponseEntity<?> disableMFA(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

        mfaService.disableMFA(user);
        return ResponseEntity.ok(Map.of(MessageConstants.KEY_MESSAGE, "MFA disabled successfully"));
    }

    /**
     * 使用恢复码禁用MFA
     *
     * @param userDetails 当前登录用户信息
     * @param recoveryCode 恢复码
     * @return 禁用成功或失败响应
     */
    @PostMapping("/disable/recovery")
    public ResponseEntity<?> disableMFAWithRecoveryCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String recoveryCode) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

        boolean disabled = mfaService.disableMFAWithRecoveryCode(user, recoveryCode);
        if (disabled) {
            return ResponseEntity.ok(Map.of(MessageConstants.KEY_MESSAGE, "MFA disabled successfully using recovery code"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(MessageConstants.KEY_MESSAGE, "Invalid recovery code"));
        }
    }

    @PostMapping("/recovery/generate")
    public ResponseEntity<?> regenerateRecoveryCode(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

        String newRecoveryCode = mfaService.regenerateRecoveryCode(user);
        return ResponseEntity.ok(Map.of(MessageConstants.KEY_RECOVERY_CODE, newRecoveryCode));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getMFAStatus(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

        Map<String, Object> response = new HashMap<>();
        response.put(MessageConstants.KEY_MFA_ENABLED, user.getMfaEnabled());
        response.put(MessageConstants.KEY_HAS_RECOVERY_CODE, user.getMfaRecoveryCode() != null);

        return ResponseEntity.ok(response);
    }
}
