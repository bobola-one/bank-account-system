package com.bobola.bank_account_system.controller;

import com.bobola.bank_account_system.dto.AccountResponse;
import com.bobola.bank_account_system.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request.accountHolderName(), request.initialBalance());
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable Long id, @RequestBody AmountRequest request) {
        return accountService.deposit(id, request.amount());
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable Long id, @RequestBody AmountRequest request) {
        return accountService.withdraw(id, request.amount());
    }

    public record CreateAccountRequest(String accountHolderName, BigDecimal initialBalance) {
    }

    public record AmountRequest(BigDecimal amount) {
    }
}