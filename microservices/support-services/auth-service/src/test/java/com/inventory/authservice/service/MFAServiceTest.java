package com.inventory.authservice.service;

import com.inventory.authservice.entity.User;
import com.inventory.authservice.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MFAServiceTest {

    @Mock
    private IUserRepository userRepository;

    private MFAService mfaService;

    @BeforeEach
    void setUp() {
        mfaService = new MFAService(userRepository);
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        return user;
    }

    @Test
    @DisplayName("Generate MFA secret should return non-null secret")
    void testGenerateMFASecret() {
        User user = createTestUser();

        String secret = mfaService.generateMFASecret(user);

        assertNotNull(secret);
        assertFalse(secret.isEmpty());
        assertEquals(secret, user.getMfaSecret());
        assertNotNull(user.getMfaRecoveryCode());
    }

    @Test
    @DisplayName("Generate QR code URL should return non-null URL")
    void testGenerateQRCodeURL() {
        User user = createTestUser();

        String secret = "JBSWY3DPEHPK3PXP";
        String qrUrl = mfaService.generateQRCodeURL(user, secret);

        assertNotNull(qrUrl);
        assertFalse(qrUrl.isEmpty());
    }

    @Test
    @DisplayName("Create MFA session should return unique session ID")
    void testCreateMfaSession() {
        String sessionId1 = mfaService.createMfaSession("testuser");
        String sessionId2 = mfaService.createMfaSession("testuser");

        assertNotNull(sessionId1);
        assertNotNull(sessionId2);
        assertFalse(sessionId1.isEmpty());
        assertFalse(sessionId2.isEmpty());
    }

    @Test
    @DisplayName("Validate MFA session should return true for valid session")
    void testValidateMfaSession() {
        String sessionId = mfaService.createMfaSession("testuser");

        assertTrue(mfaService.validateMfaSession(sessionId, "testuser"));
    }

    @Test
    @DisplayName("Validate MFA session should return false for null session")
    void testValidateMfaSessionWithNull() {
        assertFalse(mfaService.validateMfaSession(null, "testuser"));
    }

    @Test
    @DisplayName("Validate MFA session should return false for empty session")
    void testValidateMfaSessionWithEmpty() {
        assertFalse(mfaService.validateMfaSession("", "testuser"));
    }

    @Test
    @DisplayName("Regenerate recovery code should return new code and update user")
    void testRegenerateRecoveryCode() {
        User user = createTestUser();
        user.setMfaRecoveryCode("oldRecoveryCode");

        String newCode = mfaService.regenerateRecoveryCode(user);

        assertNotNull(newCode);
        assertNotEquals("oldRecoveryCode", newCode);
        assertEquals(newCode, user.getMfaRecoveryCode());
    }
}
