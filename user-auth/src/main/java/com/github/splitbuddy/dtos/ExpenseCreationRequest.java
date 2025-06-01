package com.github.splitbuddy.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseCreationRequest {

    @NotBlank(message = "Expense description is required")
    private String description;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double totalAmount;

    @NotBlank(message = "Paid by email is required")
    private String paidBy;

    @Valid
    @NotNull(message = "Shares cannot be null")
    @Size(min = 2, message = "At least two members are required to split the expense")
    private List<IndividualShare> shares;
}