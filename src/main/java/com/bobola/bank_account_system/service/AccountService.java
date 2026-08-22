package com.bobola.bank_account_system.service;

import com.bobola.bank_account_system.dto.AccountResponse;
import com.bobola.bank_account_system.dto.TransactionResponse;
import com.bobola.bank_account_system.entity.Account;
import com.bobola.bank_account_system.entity.Transaction;
import com.bobola.bank_account_system.entity.TransactionType;
import com.bobola.bank_account_system.exception.AccountNotFoundException;
import com.bobola.bank_account_system.exception.InsufficientFundsException;
import com.bobola.bank_account_system.exception.InvalidAmountException;
import com.bobola.bank_account_system.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business logic for creating accounts and performing deposits/withdrawals.
 * <p>
 * This layer owns all validation and balance rules; the repository layer
 * only knows how to read and write data, and the controller layer only
 * knows how to handle HTTP.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Constructs the service with its required repository, injected by Spring.
     *
     * @param accountRepository repository used for reading and writing accounts
     */
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Creates a new account with the given holder name and starting balance.
     *
     * @param accountHolderName name of the account holder; must not be blank
     * @param initialBalance    starting balance; must not be negative
     * @return the newly created account as a response DTO
     * @throws InvalidAmountException if the name is blank or the balance is negative
     */
    @Transactional
    public AccountResponse createAccount(String accountHolderName, BigDecimal initialBalance) {
        if (accountHolderName == null || accountHolderName.isBlank()) {
            throw new InvalidAmountException("Account holder name cannot be empty");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Initial balance cannot be negative");
        }

        Account account = new Account(accountHolderName, initialBalance);
        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    /**
     * Retrieves a single account by id.
     *
     * @param id id of the account to look up
     * @return the account as a response DTO
     * @throws AccountNotFoundException if no account exists with the given id
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        Account account = findAccountEntity(id);
        return toResponse(account);
    }

    /**
     * Retrieves every account in the system.
     *
     * @return a list of all accounts as response DTOs
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Deposits the given amount into an account and records the transaction.
     *
     * @param accountId id of the account to deposit into
     * @param amount    amount to deposit; must be greater than zero
     * @return the updated account as a response DTO
     * @throws AccountNotFoundException if no account exists with the given id
     * @throws InvalidAmountException   if the amount is null, zero, or negative
     */
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

    /**
     * Withdraws the given amount from an account and records the transaction.
     *
     * @param accountId id of the account to withdraw from
     * @param amount    amount to withdraw; must be greater than zero
     * @return the updated account as a response DTO
     * @throws AccountNotFoundException  if no account exists with the given id
     * @throws InvalidAmountException    if the amount is null, zero, or negative
     * @throws InsufficientFundsException if the account balance is less than the requested amount
     */
    @Transactional
    public AccountResponse withdraw(Long accountId, BigDecimal amount) {
        validateAmount(amount);
        Account account = findAccountEntity(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(account.getBalance(), amount);
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction transaction = new Transaction(TransactionType.WITHDRAWAL, amount, account);
        account.getTransactions().add(transaction);

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    /**
     * Looks up the raw {@link Account} entity by id, for internal use by
     * methods that need to keep working with the entity (e.g. deposit/withdraw).
     *
     * @param id id of the account to look up
     * @return the matching account entity
     * @throws AccountNotFoundException if no account exists with the given id
     */
    private Account findAccountEntity(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    /**
     * Validates that a deposit/withdrawal amount is present and positive.
     *
     * @param amount the amount to validate
     * @throws InvalidAmountException if the amount is null, zero, or negative
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    /**
     * Converts an {@link Account} entity, along with its transactions, into
     * a plain {@link AccountResponse} DTO.
     * <p>
     * This must run while the database session is still open (i.e. inside
     * a {@code @Transactional} method), since it triggers the lazy load of
     * the transactions collection.
     *
     * @param account the entity to convert
     * @return the equivalent response DTO
     */
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