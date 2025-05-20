package com.user.auth.service;

import com.user.auth.dtos.LoginRequest;
import com.user.auth.dtos.LoginResponse;
import com.user.auth.dtos.RegisterRequest;
import com.user.auth.entity.User;
import com.user.auth.enums.Role;
import com.user.auth.exception.InternalServerErrorException;
import com.user.auth.exception.InvalidCredentialsException;
import com.user.auth.exception.MissingParameterException;
import com.user.auth.exception.UserAlreadyExistsException;
import com.user.auth.repository.UserRepository;
import com.user.auth.utils.JwtUtils;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Optional.of;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationService {
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String OAUTH_2_GOOGLE_APIS_TOKEN_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=%s";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenEndpoint;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

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
                    .build();
        } catch (Exception exception) {
            log.error("Authentication failed for user: {}", request.getEmail(), exception);
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    public LoginResponse handleGoogleCallback(String code) {
        try {
            Assert.hasText(code, "Authorization code must not be empty");
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            Map.of(
                    "code", code,
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "redirect_uri", redirectUri,
                    "grant_type", AUTHORIZATION_CODE
            ).forEach(params::add);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, httpEntity, Map.class);

            var tokenId = extractParam(tokenResponse, "id_token", "ID token not found in response");

            String userInfoUrl = format(OAUTH_2_GOOGLE_APIS_TOKEN_URL, tokenId);
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
            var email = extractParam(userInfoResponse, "email", "Email not found in user info response");

            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            } catch (UsernameNotFoundException exception) {
                User user = User.builder()
                        .email(email)
                        .fullName(email.split("@")[0])
                        .role(Role.USER)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .build();
                userRepository.save(user);
            }
            var token = jwtUtils.generateToken(email);
            var refreshToken = jwtUtils.generateRefreshToken(email);
            return LoginResponse.builder().accessToken(token).refreshToken(refreshToken).build();
        } catch (Exception exception) {
            log.error("Error during Google authentication", exception);
            throw new InternalServerErrorException(exception.getMessage());
        }
    }

    private String extractParam(ResponseEntity<Map> response, String key, String errorMessage) {
        return of(response)
                .map(ResponseEntity::getBody)
                .filter(body -> body.containsKey(key))
                .map(body -> (String) body.get(key))
                .orElseThrow(() -> new MissingParameterException(errorMessage));
    }
}