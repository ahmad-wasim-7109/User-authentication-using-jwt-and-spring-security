package com.github.splitbuddy.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends SplitBuddyException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}