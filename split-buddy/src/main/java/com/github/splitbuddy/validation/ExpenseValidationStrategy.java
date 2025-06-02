package com.github.splitbuddy.validation;

import com.github.splitbuddy.dtos.ExpenseCreationRequest;
import com.github.splitbuddy.exception.InvalidDataException;

public interface ExpenseValidationStrategy {
    void validate(ExpenseCreationRequest request) throws InvalidDataException;
}

