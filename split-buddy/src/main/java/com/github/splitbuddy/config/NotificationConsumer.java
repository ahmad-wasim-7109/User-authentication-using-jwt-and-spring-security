package com.github.splitbuddy.config;

import com.github.splitbuddy.dtos.NotificationMessage;
import com.github.splitbuddy.enums.EmailTemplate;
import com.github.splitbuddy.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void handleNotification(NotificationMessage message) {
        String email = message.getEmail();
        String type = message.getType().name();
        Object[] args = message.getArgs();

        log.info("Received notification {} for {}: {}", type, email, Arrays.toString(args));

        try {
            EmailTemplate template = EmailTemplate.valueOf(type);
            emailService.sendEmail(email, template.getSubject(), String.format(template.getBody(), args));
        } catch (IllegalArgumentException e) {
            log.error("No matching EmailTemplate found for type: {}", type, e);
        } catch (Exception e) {
            log.error("Failed to process notification for email: {}", email, e);
        }
    }
}
