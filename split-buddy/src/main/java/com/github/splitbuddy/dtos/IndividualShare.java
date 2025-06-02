package com.github.splitbuddy.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IndividualShare(
        @NotBlank(message = "Owed by cannot be blank") String owedBy,
        @NotNull(message = "Amount owed cannot be null")
        Double amountOwed,
        Double percentage) {
}
