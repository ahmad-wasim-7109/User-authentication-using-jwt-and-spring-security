package com.github.splitbuddy.exception;

import lombok.Getter;

@Getter
public class SplitBuddyException extends RuntimeException {
    private final int httpStatusCode;
    private final String title;
    private final String message;

    public SplitBuddyException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.title = "Split Expense Error";
        this.message = message;
    }
}
