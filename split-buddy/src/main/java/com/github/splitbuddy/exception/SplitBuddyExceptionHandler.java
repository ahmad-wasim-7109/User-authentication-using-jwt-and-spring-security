package com.github.splitbuddy.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Collection;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
@Order(HIGHEST_PRECEDENCE)
@Slf4j
public class SplitBuddyExceptionHandler extends BaseApiExceptionHandler {

    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException methodArgumentNotValidException, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        final var bindingResult = methodArgumentNotValidException.getBindingResult();

        if (bindingResult.hasFieldErrors()) {
            log.debug("Handling MethodArgumentNotValidException for BillBuddy, error : {}", bindingResult.getFieldErrors());
            return status(BAD_REQUEST).body(bindingResult.getFieldErrors().stream()
                    .map(fieldError -> SplitBuddyAPIErrorResponse.ApiFieldErrorResponseEntry.builder()
                            .field(fieldError.getField()).message(fieldError.getDefaultMessage()).build())
                    .collect(collectingAndThen(toList(), errors -> buildErrorResponse(builder ->
                            builder.title("Validation failed")
                                    .status(BAD_REQUEST.value())
                                    .fieldErrors(errors)))));

        }
        return status(BAD_REQUEST).body(buildErrorResponse(builder ->
                builder.title("Validation failed")
                        .status(BAD_REQUEST.value())
                        .detail("looks like there should be field errors but did not find")));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<SplitBuddyAPIErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        return status(BAD_REQUEST).body(buildErrorResponse(builder -> {
            final Collection<SplitBuddyAPIErrorResponse.ApiFieldErrorResponseEntry> fieldErrors = ex.getConstraintViolations().stream()
                    .map(constraintViolation -> SplitBuddyAPIErrorResponse.ApiFieldErrorResponseEntry.builder()
                            .field(constraintViolation.getPropertyPath().toString())
                            .message(constraintViolation.getMessage())
                            .build())
                    .toList();
            return builder.fieldErrors(fieldErrors).title("Validation failed")
                    .status(BAD_REQUEST.value())
                    .detail("Check for field errors for more details");
        }));
    }

    @ExceptionHandler({InvalidDataException.class, UserNotFoundException.class, UserAlreadyExistsException.class})
    public ResponseEntity<SplitBuddyAPIErrorResponse> handleInvalidDataException(SplitBuddyException exception) {
        log.error("Invalid data exception occurred: {}", exception.getMessage());

        return status(exception.getHttpStatusCode()).body(buildErrorResponse(builder ->
                builder.title(exception.getTitle())
                        .status(exception.getHttpStatusCode())
                        .detail(exception.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SplitBuddyAPIErrorResponse> handleUnknownException(Exception ex) {
        log.error("Unknown exception occurred: {}", ex.getMessage());
        return status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse(builder ->
                builder.title("Unknown error")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .detail("An unknown error occurred. Please try again later.")));
    }
}