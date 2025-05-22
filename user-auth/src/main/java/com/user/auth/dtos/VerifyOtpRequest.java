package com.user.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record VerifyOtpRequest (
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    String userName,
    @NotBlank(message = "OTP is required")
    String otp)
{}
