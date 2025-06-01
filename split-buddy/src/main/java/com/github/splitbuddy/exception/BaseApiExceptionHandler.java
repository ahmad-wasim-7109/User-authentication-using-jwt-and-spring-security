package com.github.splitbuddy.exception;

import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.function.UnaryOperator;

import static com.github.splitbuddy.utils.RequestUtil.getCurrentRequestFullUriString;
import static com.github.splitbuddy.utils.RequestUtil.getCurrentRequestUriPath;

public abstract class BaseApiExceptionHandler extends ResponseEntityExceptionHandler {

    protected SplitBuddyAPIErrorResponse buildErrorResponse(UnaryOperator<SplitBuddyAPIErrorResponse.SplitBuddyAPIErrorResponseBuilder> builderDecorator) {
        return builderDecorator.apply(SplitBuddyAPIErrorResponse.builder()
                        .type(getCurrentRequestFullUriString())
                        .instance(getCurrentRequestUriPath()))
                .build();
    }
}
