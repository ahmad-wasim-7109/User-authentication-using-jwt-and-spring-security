package com.user.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
        }catch (Exception exception) {
            log.error("Error occurred while sending mail to: {}, exception: {}", to, exception.getMessage());
        }
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        log.info("Sending otp email to: {}, otp: {}", to, otp);
        String subject = "Verify Your Email Address";
        String body =  "Dear User,\n\n"
                + "Thank you for registering! To complete your email verification, please use the following One-Time Password (OTP):\n\n"
                + "OTP: " + otp + "\n\n"
                + "This OTP is valid for 5 minutes. If you did not request this verification, please ignore this email.\n\n"
                + "Best regards,\n"
                + "SplitBillsTeam";
        this.sendEmail(to, subject, body);
    }
}