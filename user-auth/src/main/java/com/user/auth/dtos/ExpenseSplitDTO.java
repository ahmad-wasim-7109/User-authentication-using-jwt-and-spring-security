package com.user.auth.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ExpenseSplitDTO {
    private String expenseSplitId;
    private GroupMemberDTO owedBy;
    private Double amountOwed;
    private Date SplitAt;
    private boolean isSettled;
}
