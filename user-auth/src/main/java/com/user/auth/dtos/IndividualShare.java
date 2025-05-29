package com.user.auth.dtos;


import jakarta.validation.constraints.NotBlank;

public record IndividualShare(@NotBlank(message = "Owed by cannot be blank") String owedBy, Double amountOwed) {
}
