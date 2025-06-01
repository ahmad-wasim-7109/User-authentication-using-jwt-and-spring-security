package com.github.splitbuddy.events;

import com.github.splitbuddy.enums.NotificationType;
import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final String recipientEmail;
    private final NotificationType type;
    private final Object[] args;

    public NotificationEvent(Object source, String recipientEmail, NotificationType type, Object... args) {
        super(source);
        this.recipientEmail = recipientEmail;
        this.type = type;
        this.args = args;
    }
}