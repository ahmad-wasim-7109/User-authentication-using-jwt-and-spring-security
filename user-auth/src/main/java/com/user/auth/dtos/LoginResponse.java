package com.user.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
