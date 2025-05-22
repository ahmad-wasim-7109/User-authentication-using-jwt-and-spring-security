package com.user.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 50, message = "Full name should be between 3 and 50 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 50, message = "Email should not exceed 50 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password should be between 8 and 20 characters")
        String password,

        @NotBlank(message = "Role is required")
        String role)
{}