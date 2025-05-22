package com.user.auth.controller;

import com.user.auth.dtos.*;
import com.user.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        RegisterResponse registerResponse = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping(value = "/google-callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> handleGoogleCallback(@RequestParam String code) {
        LoginResponse loginResponse = authenticationService.handleGoogleCallback(code);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping(value = "/verify-otp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyOtp(@RequestBody @Valid VerifyOtpRequest verifyOtpRequest) {
        authenticationService.verifyOtp(verifyOtpRequest);
        return ResponseEntity.ok("OTP verified successfully");
    }

    @GetMapping(value = "/resend-otp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> resendOtp(@RequestParam String userName) {
        authenticationService.resendOtp(userName);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        LoginResponse loginResponse = authenticationService.refreshToken(refreshTokenRequest.refreshToken());
        return ResponseEntity.ok(loginResponse);
    }
}