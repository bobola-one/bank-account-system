package com.bobola.bank_account_system.service;

import com.bobola.bank_account_system.entity.Account;
import com.bobola.bank_account_system.exception.AccountNotFoundException;
import com.bobola.bank_account_system.exception.InsufficientFundsException;
import com.bobola.bank_account_system.exception.InvalidAmountException;
import com.bobola.bank_account_system.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AccountService}.
 * <p>
 * The {@link AccountRepository} is mocked so these tests exercise only the
 * business logic in {@code AccountService} itself, without touching a real
 * database.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    /**
     * Constructs a fresh {@link AccountService} with the mocked repository
     * before each test, so tests don't affect each other's state.
     */
    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
    }

    /**
     * Verifies that creating an account with valid input succeeds and
     * returns the expected values.
     */
    @Test
    void createAccount_withValidInput_returnsCreatedAccount() {
        Account savedAccount = new Account("Bobola", new BigDecimal("100.00"));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        var result = accountService.createAccount("Bobola", new BigDecimal("100.00"));

        assertEquals("Bobola", result.accountHolderName());
        assertEquals(new BigDecimal("100.00"), result.balance());
    }

    /**
     * Verifies that creating an account with a negative initial balance
     * is rejected before the repository is ever called.
     */
    @Test
    void createAccount_withNegativeBalance_throwsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () ->
                accountService.createAccount("Bobola", new BigDecimal("-50.00")));
    }

    /**
     * Verifies that depositing a valid amount increases the account balance.
     */
    @Test
    void deposit_withValidAmount_increasesBalance() {
        Account account = new Account("Bobola", new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        var result = accountService.deposit(1L, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), result.balance());
    }

    /**
     * Verifies that depositing a zero or negative amount is rejected.
     */
    @Test
    void deposit_withZeroAmount_throwsInvalidAmountException() {
        assertThrows(InvalidAmountException.class, () ->
                accountService.deposit(1L, BigDecimal.ZERO));
    }

    /**
     * Verifies that depositing into a nonexistent account throws
     * {@link AccountNotFoundException} rather than a generic error.
     */
    @Test
    void deposit_withNonexistentAccount_throwsAccountNotFoundException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.deposit(99L, new BigDecimal("50.00")));
    }

    /**
     * Verifies that withdrawing a valid amount within the balance succeeds
     * and decreases the balance correctly.
     */
    @Test
    void withdraw_withSufficientFunds_decreasesBalance() {
        Account account = new Account("Bobola", new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        var result = accountService.withdraw(1L, new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), result.balance());
    }

    /**
     * Verifies that attempting to withdraw more than the current balance
     * throws {@link InsufficientFundsException} and does not save any change.
     */
    @Test
    void withdraw_withAmountExceedingBalance_throwsInsufficientFundsException() {
        Account account = new Account("Bobola", new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () ->
                accountService.withdraw(1L, new BigDecimal("500.00")));
    }
}