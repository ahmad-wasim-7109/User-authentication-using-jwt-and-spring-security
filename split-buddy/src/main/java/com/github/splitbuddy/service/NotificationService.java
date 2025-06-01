package com.github.splitbuddy.service;

import com.github.splitbuddy.enums.NotificationType;
import com.github.splitbuddy.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ApplicationEventPublisher eventPublisher;

    public void notifyUser(NotificationType type, String email, Object... args) {
        eventPublisher.publishEvent(new NotificationEvent(this, email, type, args));
    }
}