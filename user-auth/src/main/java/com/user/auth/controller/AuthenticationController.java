package com.user.auth.controller;

import com.user.auth.dtos.LoginRequest;
import com.user.auth.dtos.LoginResponse;
import com.user.auth.dtos.RefreshTokenRequest;
import com.user.auth.dtos.RegisterRequest;
import com.user.auth.dtos.RegisterResponse;
import com.user.auth.dtos.VerifyOtpRequest;
import com.user.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = RegisterResponse.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request")})
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        RegisterResponse registerResponse = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @Operation(summary = "Login a user")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = LoginResponse.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request")})

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Login a user with Google")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = LoginResponse.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request")})

    @GetMapping(value = "/google-callback", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> handleGoogleCallback(@RequestParam String code) {
        LoginResponse loginResponse = authenticationService.handleGoogleCallback(code);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Verify OTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid OTP or request data")
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verifyOtp(@RequestBody @Valid VerifyOtpRequest verifyOtpRequest) {
        authenticationService.verifyOtp(verifyOtpRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Resend OTP to the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "OTP resent successfully with no content returned"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid or missing parameters")
    })
    @GetMapping("/resend-otp")
    public ResponseEntity<Void> resendOtp(@RequestParam String userName) {
        authenticationService.resendOtp(userName);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generate jwt token from refresh token")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = LoginResponse.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request")})
    @PostMapping(value = "/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        LoginResponse loginResponse = authenticationService.refreshToken(refreshTokenRequest.refreshToken());
        return ResponseEntity.ok(loginResponse);
    }
}