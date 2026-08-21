package com.bobola.bank_account_system.exception;

/**
 * Thrown when an account lookup fails because no account exists
 * with the given id.
 * <p>
 * Mapped to HTTP 404 Not Found by the global exception handler.
 */
public class AccountNotFoundException extends RuntimeException {

    /**
     * Constructs the exception with a message built from the missing account id.
     *
     * @param accountId the id that was searched for but not found
     */
    public AccountNotFoundException(Long accountId) {
        super("Account not found with id: " + accountId);
    }
}