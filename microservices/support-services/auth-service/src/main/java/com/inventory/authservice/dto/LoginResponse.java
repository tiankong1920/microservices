package com.inventory.authservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String username;
    private List<String> roles;
    private boolean mfaRequired;
    private String mfaSessionId;
}
