package com.user.auth.dtos;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final HttpStatus status;
    private final String message;
    private final LocalDateTime timestamp;
    private final String path;

    public ErrorResponse(HttpStatus status, String message, String path) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    public ErrorResponse(HttpStatus status, String message, String path, String errorCode) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }
}
