package com.github.splitbuddy.exception;

import org.springframework.http.HttpStatus;

public class InvalidDataException extends SplitBuddyException{
    public InvalidDataException(String message) {
        super(message, HttpStatus.BAD_REQUEST.value());
    }
}
