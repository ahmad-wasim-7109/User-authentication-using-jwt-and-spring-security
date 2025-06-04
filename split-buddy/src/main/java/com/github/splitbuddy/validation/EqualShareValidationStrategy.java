package com.github.splitbuddy.validation;

import com.github.splitbuddy.dtos.ExpenseCreationRequest;
import com.github.splitbuddy.exception.InvalidDataException;

public class EqualShareValidationStrategy implements ExpenseValidationStrategy {
    @Override
    public void validate(ExpenseCreationRequest request) throws InvalidDataException {
        double equalShare = request.getTotalAmount() / request.getShares().size();
        for (final var share : request.getShares()) {
            if (Math.abs(share.amountOwed() - equalShare) > 0.01) {
                throw new InvalidDataException("For equal split, all shares must be exactly " + equalShare);
            }
            if (share.percentage() != null) {
                throw new InvalidDataException("Percentage should not be provided for equal split");
            }
        }
    }
}
