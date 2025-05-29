package com.user.auth.converter;

import com.user.auth.dtos.ExpenseCreationRequest;
import com.user.auth.dtos.ExpenseSplitDTO;
import com.user.auth.dtos.IndividualShare;
import com.user.auth.entity.Expense;
import com.user.auth.entity.ExpenseSplit;
import com.user.auth.enums.SettlementStatus;

import java.util.Date;
import java.util.UUID;

public class ExpenseSplitConverter {

    public static ExpenseSplit convertToExpenseSplit(ExpenseCreationRequest expenseCreationRequest,
                                                     IndividualShare share, Expense expense) {
        return ExpenseSplit.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .expense(expense)
                .amountOwed(share.amountOwed())
                .owedBy(share.owedBy())
                .status(expenseCreationRequest.getPaidBy().equals(share.owedBy()) ? SettlementStatus.SETTLED : SettlementStatus.PENDING)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    public static ExpenseSplitDTO convertToExpenseSplitDTO(ExpenseSplit expenseSplit) {
        return ExpenseSplitDTO.builder()
                .expenseId(expenseSplit.getExpense().getId())
                .expenseSplitId(expenseSplit.getId())
                .owedBy(expenseSplit.getOwedBy())
                .amountOwed(expenseSplit.getAmountOwed())
                .splitAt(expenseSplit.getCreatedAt())
                .isSettled(expenseSplit.getStatus().equals(SettlementStatus.SETTLED))
                .build();
    }
}
