package com.user.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ExpenseDTO {
    private String expenseId;
    private String description;
    private Double ExpenseAmount;
    private String paidBy;
    private String createdBy;
    private Date createdAt;
    private List<ExpenseSplitDTO> expenseSplits;
}
