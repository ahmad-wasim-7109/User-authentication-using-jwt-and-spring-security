package com.user.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RequestMapping("/api/v1/auth/google")
@RestController
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenEndpoint;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code) {
        try {
            // Prepare the request parameters
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Create the HTTP entity
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

            // Send POST request to Google's token endpoint
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, httpEntity, Map.class);

            // Extract the ID token
            Map responseBody = tokenResponse.getBody();
            if (responseBody == null || !responseBody.containsKey("id_token")) {
                throw new RuntimeException("ID token not found in response");
            }
            String idToken = (String) responseBody.get("id_token");

            // Corrected user info URL
            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            // Log user info
            log.info("User info: {}", userInfoResponse.getBody());

            return ResponseEntity.ok(userInfoResponse.getBody());
        } catch (Exception e) {
            log.error("Error handling Google callback: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error handling Google callback: " + e.getMessage());
        }
    }
}
