package com.github.splitbuddy.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Balance {
    String email;
    double amount;
}
