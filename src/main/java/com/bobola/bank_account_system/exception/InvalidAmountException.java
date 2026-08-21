package com.bobola.bank_account_system.exception;

/**
 * Thrown when a deposit or withdrawal amount fails basic validation
 * (null, zero, or negative).
 * <p>
 * Mapped to HTTP 400 Bad Request by the global exception handler, since
 * the client sent malformed input.
 */
public class InvalidAmountException extends RuntimeException {

    /**
     * Constructs the exception with a specific validation failure message.
     *
     * @param message description of why the amount was rejected
     */
    public InvalidAmountException(String message) {
        super(message);
    }
}