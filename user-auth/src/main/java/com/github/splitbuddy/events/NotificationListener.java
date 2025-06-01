package com.github.splitbuddy.events;

import com.github.splitbuddy.enums.EmailTemplate;
import com.github.splitbuddy.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
@EnableAsync
@RequiredArgsConstructor
public class NotificationListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        EmailTemplate template = EmailTemplate.valueOf(event.getType().name());
        String subject = template.getSubject();
        String body = format(template.getBody(), event.getArgs());

        emailService.sendEmail(event.getRecipientEmail(), subject, body);
    }
}