package com.user.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyOtpRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String userName;
    @NotBlank(message = "OTP is required")
    private String otp;
}
