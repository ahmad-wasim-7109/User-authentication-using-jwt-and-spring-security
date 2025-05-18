package com.user.auth.dtos;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final HttpStatus status;
    private final String message;
    private final LocalDateTime timestamp;
    private final String path;

    public ErrorResponse(HttpStatus status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}