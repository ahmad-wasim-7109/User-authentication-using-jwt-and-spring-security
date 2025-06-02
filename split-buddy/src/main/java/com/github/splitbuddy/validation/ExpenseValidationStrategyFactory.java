package com.github.splitbuddy.validation;

import com.github.splitbuddy.enums.SplitType;

public class ExpenseValidationStrategyFactory {
    public static ExpenseValidationStrategy getStrategy(SplitType type) {
        return switch (type) {
            case EQUAL -> new EqualShareValidationStrategy();
            case UNEQUAL -> new UnequalShareValidationStrategy();
            case PERCENTAGE -> new PercentageBasedShareValidationStrategy();
            default -> throw new IllegalArgumentException("Unknown split type: " + type);
        };
    }
}
