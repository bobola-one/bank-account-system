package com.bobola.bank_account_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AccountResponse(
        Long id,
        String accountHolderName,
        BigDecimal balance,
        LocalDateTime createdAt,
        List<TransactionResponse> transactions
) {
}