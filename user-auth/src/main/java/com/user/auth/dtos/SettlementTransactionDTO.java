package com.user.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SettlementTransactionDTO {
    private String fromUser;
    private String toUser;
    private Double amount;
}