package com.github.splitbuddy.converter;

import com.github.splitbuddy.dtos.ExpenseDTO;
import com.github.splitbuddy.dtos.ExpenseSplitDTO;
import com.github.splitbuddy.dtos.IndividualShare;
import com.github.splitbuddy.entity.Expense;
import com.github.splitbuddy.entity.ExpenseSplit;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExpenseSplitConverter {

    public static ExpenseSplit convertToExpenseSplit(IndividualShare share, Expense expense) {
        return ExpenseSplit.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .expense(expense)
                .amountOwed(share.amountOwed())
                .owedBy(share.owedBy())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    public static ExpenseSplitDTO convertToExpenseSplitDTO(ExpenseSplit expenseSplit) {
        return ExpenseSplitDTO.builder()
                .expenseSplitId(expenseSplit.getId())
                .owedBy(expenseSplit.getOwedBy())
                .amountOwed(expenseSplit.getAmountOwed())
                .splitAt(expenseSplit.getCreatedAt())
                .build();
    }

    public static List<ExpenseDTO> convertToExpenseDTOs(List<Expense> expenses) {
        return expenses.stream()
                .map(expense -> {
                    ExpenseDTO dto = ExpenseConverter.convertToExpenseDTO(expense);
                    List<ExpenseSplitDTO> splitDTOs = expense.getSplits().stream()
                            .map(ExpenseSplitConverter::convertToExpenseSplitDTO)
                            .toList();
                    dto.setExpenseSplits(splitDTOs);
                    return dto;
                })
                .toList();
    }
}
