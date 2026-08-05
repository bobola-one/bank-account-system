package com.bobola.bank_account_system.dto;

import com.bobola.bank_account_system.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        LocalDateTime timestamp
) {
}