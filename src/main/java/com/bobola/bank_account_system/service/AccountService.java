package com.bobola.bank_account_system.service;

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
    public Account createAccount(String accountHolderName, BigDecimal initialBalance) {
        if (accountHolderName == null || accountHolderName.isBlank()) {
            throw new IllegalArgumentException("Account holder name cannot be empty");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        Account account = new Account(accountHolderName, initialBalance);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        validateAmount(amount);

        Account account = getAccountById(accountId);

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(TransactionType.DEPOSIT, amount, account);
        account.getTransactions().add(transaction);

        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(Long accountId, BigDecimal amount) {
        validateAmount(amount);

        Account account = getAccountById(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds: balance is " + account.getBalance()
                    + " but withdrawal amount is " + amount);
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction(TransactionType.WITHDRAWAL, amount, account);
        account.getTransactions().add(transaction);

        return accountRepository.save(account);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}