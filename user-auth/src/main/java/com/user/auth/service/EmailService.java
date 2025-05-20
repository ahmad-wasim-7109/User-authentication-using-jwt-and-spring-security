package com.user.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableAsync
public class EmailService {
    @Async
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {}", to);
    }

    @Async
    public void sendOtpEmail(String to, String subject, String body) {
        log.info("Sending otp email to: {}, otp: {}", to, body);
    }
}