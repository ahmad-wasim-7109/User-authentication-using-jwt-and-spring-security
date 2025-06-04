package com.github.splitbuddy.dtos;

import com.github.splitbuddy.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationMessage {
    private NotificationType type;
    private String email;
    private Object[] args;
}
