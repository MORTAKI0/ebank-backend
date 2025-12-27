package com.ebank.backend.repository;

import com.ebank.backend.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByRib(String rib);
    boolean existsByRib(String rib);
    List<BankAccount> findByCustomerId(Long customerId);
}
