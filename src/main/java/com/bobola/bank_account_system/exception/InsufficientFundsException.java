package com.bobola.bank_account_system.exception;

import java.math.BigDecimal;

/**
 * Thrown when a withdrawal is attempted for an amount greater than
 * the account's current balance.
 * <p>
 * Mapped to HTTP 409 Conflict by the global exception handler, since
 * the request itself is well-formed but conflicts with the account's
 * current state.
 */
public class InsufficientFundsException extends RuntimeException {

    /**
     * Constructs the exception with a message describing the shortfall.
     *
     * @param balance         the account's current balance
     * @param requestedAmount the amount that was requested to be withdrawn
     */
    public InsufficientFundsException(BigDecimal balance, BigDecimal requestedAmount) {
        super("Insufficient funds: balance is " + balance + " but withdrawal amount is " + requestedAmount);
    }
}