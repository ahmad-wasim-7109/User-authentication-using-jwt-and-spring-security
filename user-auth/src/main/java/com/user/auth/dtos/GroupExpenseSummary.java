package com.user.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupExpenseSummary {
    List<GroupExpenseDTO> groupExpenseSummary;
}