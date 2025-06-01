package com.github.splitbuddy.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends SplitBuddyException {
    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED.value());
    }
}
