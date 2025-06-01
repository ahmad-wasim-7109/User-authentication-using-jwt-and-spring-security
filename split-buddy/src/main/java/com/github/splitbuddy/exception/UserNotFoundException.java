package com.github.splitbuddy.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends SplitBuddyException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }
}
