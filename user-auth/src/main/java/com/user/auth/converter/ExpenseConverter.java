package com.user.auth.converter;

import com.user.auth.dtos.ExpenseCreationRequest;
import com.user.auth.dtos.ExpenseDTO;
import com.user.auth.dtos.GroupExpenseDTO;
import com.user.auth.entity.Expense;
import com.user.auth.entity.Group;
import com.user.auth.entity.User;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExpenseConverter {

    public static Expense convertToExpense(Group group, ExpenseCreationRequest request, User user) {
        return Expense.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .paidBy(request.getPaidBy())
                .createdBy(user)
                .group(group)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    public static ExpenseDTO convertToExpenseDTO(Expense expense) {
        return ExpenseDTO.builder()
                .expenseId(expense.getId())
                .description(expense.getDescription())
                .ExpenseAmount(expense.getTotalAmount())
                .paidBy(expense.getPaidBy())
                .createdBy(expense.getCreatedBy().getEmail())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
