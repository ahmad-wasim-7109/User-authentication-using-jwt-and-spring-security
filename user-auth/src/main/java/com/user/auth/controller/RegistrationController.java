package com.user.auth.controller;

import com.user.auth.dtos.LoginRequest;
import com.user.auth.dtos.LoginResponse;
import com.user.auth.dtos.RegisterRequest;
import com.user.auth.dtos.RegisterResponse;
import com.user.auth.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class RegistrationController {
    private final RegistrationService authenticationService;

    // Endpoint to register a new user
    @PostMapping(value = "/register", produces = "application/json")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse("User registered successfully"));
    }

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);
        return ResponseEntity.ok(loginResponse);
    }
}