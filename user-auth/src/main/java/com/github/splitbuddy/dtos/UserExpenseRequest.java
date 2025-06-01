package com.github.splitbuddy.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExpenseRequest {
    private String email;
    private String description;
    private Double amount;
    private String paidBy;
    private String createdBy;
    @Singular
    private List<IndividualShare> shares;
}
