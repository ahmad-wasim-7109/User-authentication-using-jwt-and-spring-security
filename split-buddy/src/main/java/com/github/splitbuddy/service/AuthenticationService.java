package com.github.splitbuddy.service;

import com.github.splitbuddy.dao.UserRepository;
import com.github.splitbuddy.dtos.*;
import com.github.splitbuddy.entity.User;
import com.github.splitbuddy.enums.Role;
import com.github.splitbuddy.exception.InvalidDataException;
import com.github.splitbuddy.exception.SplitBuddyException;
import com.github.splitbuddy.exception.UserAlreadyExistsException;
import com.github.splitbuddy.utils.JwtUtils;
import com.github.splitbuddy.utils.SplitUtil;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

import static com.github.splitbuddy.converter.GroupMemberConverter.userToGroupMemberDTO;
import static com.github.splitbuddy.enums.NotificationType.OTP_GENERATED;
import static com.github.splitbuddy.utils.HashUtil.sha256Hex;
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
    private final RedisService redisService;
    private final NotificationService notificationService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenEndpoint;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;
    @Value("${jwt.refresh.token.expiration}")
    long JWT_REFRESH_EXPIRATION;

    public RegisterResponse register(RegisterRequest request) {

        userRepository.findByEmail(request.email()).ifPresent(user -> {
            if (user.getIsEmailVerified()) {
                throw new UserAlreadyExistsException("User with this email already exists");
            } else {
                userRepository.delete(user);
            }
        });
        User user = createUser(request);
        userRepository.save(user);
        return buildRegistrationResponse(request);
    }

    private RegisterResponse buildRegistrationResponse(RegisterRequest request) {
        return RegisterResponse.builder()
                .message("User registered successfully")
                .userName(request.email())
                .fullName(request.fullName())
                .isEmailVerified(false)
                .build();
    }

    private User createUser(RegisterRequest request) {
        return User.builder()
                .id(SplitUtil.generateUUID())
                .email(request.email())
                .fullName(request.fullName())
                .password(passwordEncoder.encode(request.password()))
                .isEmailVerified(false)
                .role(Role.valueOf(request.role()))
                .build();
    }

    public LoginResponse login(LoginRequest request) throws AuthenticationException {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            final var user = (User) authentication.getPrincipal();

            if (!user.getIsEmailVerified()) {
                return LoginResponse.builder()
                        .user(userToGroupMemberDTO(user))
                        .build();
            }

            var token = jwtUtils.generateToken(user.getUsername());
            var refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
            final var redisRefreshKey = sha256Hex(user.getEmail() + "_refresh_token");
            redisService.put(redisRefreshKey, refreshToken,
                    new Date(System.currentTimeMillis() + JWT_REFRESH_EXPIRATION).getTime());
            return LoginResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .user(userToGroupMemberDTO(user))
                    .build();
        } catch (AuthenticationException e) {
            throw new InvalidDataException("username/password is not correct");
        } catch (Exception e) {
            log.error("Error while logging in", e);
            throw new SplitBuddyException("Error while logging in", HttpStatus.INTERNAL_SERVER_ERROR.value());
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

            final var tokenId = extractParam(tokenResponse, "id_token", "ID token not found in response");

            String userInfoUrl = format(OAUTH_2_GOOGLE_APIS_TOKEN_URL, tokenId);
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
            final var email = extractParam(userInfoResponse, "email", "Email not found in user info response");

            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            } catch (UsernameNotFoundException exception) {
                User user = User.builder().email(email).fullName(email.split("@")[0]).role(Role.USER)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString())).build();
                userRepository.save(user);
            }
            final var token = jwtUtils.generateToken(email);
            final var refreshToken = jwtUtils.generateRefreshToken(email);
            return LoginResponse.builder().accessToken(token).refreshToken(refreshToken).build();
        } catch (Exception exception) {
            log.error("Error during Google authentication", exception);
            throw new SplitBuddyException(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String extractParam(ResponseEntity<Map> response, String key, String errorMessage) {
        return of(response)
                .map(ResponseEntity::getBody)
                .filter(body -> body.containsKey(key))
                .map(body -> (String) body.get(key))
                .orElseThrow(() -> new InvalidDataException("missing parameter"));
    }

    public String generateAndStoreOtpInRedis(String userName) {
        SecureRandom secureRandom = new SecureRandom();
        final var otpValue = secureRandom.nextInt(900000) + 100000;
        final var otp = String.valueOf(otpValue);
        final var safeKey = sha256Hex(userName + "_otp");
        redisService.put(safeKey, otp, 5 * 60 * 1000);
        return otp;
    }

    public void verifyOtp(VerifyOtpRequest otpRequest) {
        final var redisKey = sha256Hex(otpRequest.userName() + "_otp");
        final var storedOtp = redisService.get(redisKey);

        if (storedOtp == null || !MessageDigest.isEqual(
                storedOtp.getBytes(StandardCharsets.UTF_8),
                otpRequest.otp().getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidDataException("Invalid or expired OTP");
        }

        userRepository.findByEmail(otpRequest.userName())
                .ifPresent(user -> {
                    user.setIsEmailVerified(true);
                    userRepository.save(user);
                    redisService.delete(redisKey);
                });
    }

    public void resendOtp(String email) {
        Assert.hasText(email, "Email must not be empty");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new InvalidDataException("Email not found");
        }
        User user = userOpt.get();
        if (user.getIsEmailVerified()) {
            throw new InvalidDataException("Email is already verified");
        }
        final var otp = generateAndStoreOtpInRedis(email);
        notificationService.notifyUser(OTP_GENERATED, email, otp);
    }

    public LoginResponse refreshToken(String refreshToken) {
        Assert.hasText(refreshToken, "Refresh token must not be empty");
        try {
            final var userName = jwtUtils.extractUsername(refreshToken);
            if (userName == null) {
                throw new InvalidDataException("Invalid refresh token");
            }
            validateRefreshToken(userName, refreshToken);
            String newAccessToken = jwtUtils.generateToken(userName);
            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (JwtException e) {
            log.error("JWT error during refresh token", e);
            throw new InvalidDataException("Invalid refresh token");
        } catch (Exception e) {
            log.error("Unexpected error during refresh token", e);
            throw new SplitBuddyException("Unexpected error during refresh token", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void validateRefreshToken(String userName, String refreshToken) {

        if (!jwtUtils.isTokenValid(refreshToken)) {
            throw new InvalidDataException("Refresh token has expired");
        }
        final var refreshKey = sha256Hex(userName + "_refresh_token");
        String storedToken = redisService.get(refreshKey);

        if (!Objects.equals(storedToken, refreshToken)) {
            throw new InvalidDataException("Refresh token mismatch or not found");
        }
    }
}