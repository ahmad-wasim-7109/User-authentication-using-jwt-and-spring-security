package com.user.auth.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class GroupExpenseDTO {
    private String id;
    private String groupName;
    private String description;
    private String createdBy;
    private Date createdAt;
    private Double expenseAmount;
    private List<GroupMemberDTO> members;
    private List<ExpenseDTO> expenseSplits;
}
