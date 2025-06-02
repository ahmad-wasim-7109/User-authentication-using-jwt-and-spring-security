package com.github.splitbuddy.validation;

import com.github.splitbuddy.dtos.ExpenseCreationRequest;
import com.github.splitbuddy.exception.InvalidDataException;

public class UnequalShareValidationStrategy implements ExpenseValidationStrategy {
    @Override
    public void validate(ExpenseCreationRequest request) throws InvalidDataException {
        double sum = request.getShares().stream()
                .mapToDouble(s -> s.amountOwed() == null ? 0 : s.amountOwed())
                .sum();

        if (Math.abs(sum - request.getTotalAmount()) > 0.01) {
            throw new InvalidDataException("Sum of shares must match total amount");
        }

        for (final var share : request.getShares()) {
            if (share.percentage() != null) {
                throw new InvalidDataException("Percentage should not be provided for unequal split");
            }
        }
    }
}
