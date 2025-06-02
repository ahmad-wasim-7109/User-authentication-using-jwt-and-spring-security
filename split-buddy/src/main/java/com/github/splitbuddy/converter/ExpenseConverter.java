package com.github.splitbuddy.converter;

import com.github.splitbuddy.dtos.ExpenseCreationRequest;
import com.github.splitbuddy.dtos.ExpenseDTO;
import com.github.splitbuddy.entity.Expense;
import com.github.splitbuddy.entity.Group;
import com.github.splitbuddy.entity.User;

import java.util.Date;
import java.util.UUID;

import static com.github.splitbuddy.utils.SplitUtil.generateUUID;

public class ExpenseConverter {

    public static Expense convertToExpense(Group group, ExpenseCreationRequest request, User user) {
        return Expense.builder()
                .id(generateUUID())
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
