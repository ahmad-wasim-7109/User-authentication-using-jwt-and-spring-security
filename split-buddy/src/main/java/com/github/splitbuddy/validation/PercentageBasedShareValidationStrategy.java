package com.github.splitbuddy.validation;

import com.github.splitbuddy.dtos.ExpenseCreationRequest;
import com.github.splitbuddy.exception.InvalidDataException;

public class PercentageBasedShareValidationStrategy implements ExpenseValidationStrategy {
    @Override
    public void validate(ExpenseCreationRequest request) throws InvalidDataException {
        double totalPercentage = request.getShares().stream()
                .mapToDouble(s -> s.percentage() == null ? 0 : s.percentage())
                .sum();

        if (Math.abs(totalPercentage - 100.0) > 0.01) {
            throw new InvalidDataException("Percentages must add up to 100%");
        }

        for (final var share : request.getShares()) {
            if (share.percentage() == null || share.percentage() < 0) {
                throw new InvalidDataException("Each share must have a positive percentage");
            }
            if (Math.abs(share.percentage() * request.getTotalAmount() / 100 - share.amountOwed()) > 0.01) {
                throw new InvalidDataException("Share amount must be equal to percentage * total amount / 100");
            }
        }
    }
}
