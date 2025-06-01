package com.github.splitbuddy.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupExpenseSummary {
    private Double totalSettlementAmount;
    List<GroupExpenseDTO> groupExpenseSummary;
}