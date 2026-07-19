package com.bobola.bank_account_system.repository;

import com.bobola.bank_account_system.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}