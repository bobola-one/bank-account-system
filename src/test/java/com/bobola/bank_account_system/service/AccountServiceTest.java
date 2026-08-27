package com.bobola.bank_account_system.service;

import com.bobola.bank_account_system.entity.Account;
import com.bobola.bank_account_system.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(accountRepository.save(org.mockito.ArgumentMatchers.any(Account.class)))
                .thenReturn(savedAccount);

        var result = accountService.createAccount("Bobola", new BigDecimal("100.00"));

        assertEquals("Bobola", result.accountHolderName());
        assertEquals(new BigDecimal("100.00"), result.balance());
    }
}