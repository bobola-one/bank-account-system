package com.bobola.bank_account_system.service;

import com.bobola.bank_account_system.dto.AccountResponse;
import com.bobola.bank_account_system.dto.TransactionResponse;
import com.bobola.bank_account_system.entity.Account;
import com.bobola.bank_account_system.entity.Transaction;
import com.bobola.bank_account_system.entity.TransactionType;
import com.bobola.bank_account_system.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(String accountHolderName, BigDecimal initialBalance) {
        if (accountHolderName == null || accountHolderName.isBlank()) {
            throw new IllegalArgumentException("Account holder name cannot be empty");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        Account account = new Account(accountHolderName, initialBalance);
        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AccountResponse deposit(Long accountId, BigDecimal amount) {
        validateAmount(amount);
        Account account = findAccountEntity(accountId);

        account.setBalance(account.getBalance().add(amount));

        Transaction transaction = new Transaction(TransactionType.DEPOSIT, amount, account);
        account.getTransactions().add(transaction);

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional
    public AccountResponse withdraw(Long accountId, BigDecimal amount) {
        validateAmount(amount);
        Account account = findAccountEntity(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds: balance is " + account.getBalance()
                    + " but withdrawal amount is " + amount);
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction transaction = new Transaction(TransactionType.WITHDRAWAL, amount, account);
        account.getTransactions().add(transaction);

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    private Account findAccountEntity(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private AccountResponse toResponse(Account account) {
        List<TransactionResponse> transactionResponses = account.getTransactions()
                .stream()
                .map(t -> new TransactionResponse(t.getId(), t.getType(), t.getAmount(), t.getTimestamp()))
                .toList();

        return new AccountResponse(
                account.getId(),
                account.getAccountHolderName(),
                account.getBalance(),
                account.getCreatedAt(),
                transactionResponses
        );
    }
}