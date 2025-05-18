package com.user.auth.service;

import com.user.auth.dtos.LoginRequest;
import com.user.auth.dtos.LoginResponse;
import com.user.auth.dtos.RegisterRequest;
import com.user.auth.entity.User;
import com.user.auth.enums.Role;
import com.user.auth.exception.InvalidCredentialsException;
import com.user.auth.exception.UserAlreadyExistsException;
import com.user.auth.repository.UserRepository;
import com.user.auth.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    // Method to register a new user
    public void register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .build();
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            var user = (User) authentication.getPrincipal();
            var token = jwtUtils.generateToken(user.getUsername());
            var refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
            return LoginResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .accessTokenExpiry(jwtUtils.extractExpiration(token))
                    .refreshTokenExpiry(jwtUtils.extractExpiration(refreshToken))
                    .build();
        } catch (Exception exception) {
            log.error("Authentication failed for user: {}", request.getEmail(), exception);
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
}