package com.github.splitbuddy.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String message;
    private String userName;
    private String fullName;
    private boolean isEmailVerified;
}