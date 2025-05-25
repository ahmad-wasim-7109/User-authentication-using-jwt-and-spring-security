package com.user.auth.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class ExpenseDTO {
    private String expenseId;
    private String description;
    private Double ExpenseAmount;
    private GroupMemberDTO paidBy;
    private String createdBy;
    private Date createdAt;
    private List<ExpenseSplitDTO> expenseSplits;
}
