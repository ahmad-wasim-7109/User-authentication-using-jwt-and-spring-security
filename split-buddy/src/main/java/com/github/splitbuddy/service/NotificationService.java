package com.github.splitbuddy.service;

import com.github.splitbuddy.dtos.NotificationMessage;
import com.github.splitbuddy.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;

    public void notifyUser(NotificationType type, String email, Object... args) {
        NotificationMessage message = new NotificationMessage(type, email, args);
        kafkaTemplate.send("notifications", message);
    }
}