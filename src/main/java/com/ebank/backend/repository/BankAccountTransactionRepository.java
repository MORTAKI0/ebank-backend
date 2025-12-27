package com.ebank.backend.repository;

import com.ebank.backend.entity.BankAccountTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountTransactionRepository extends JpaRepository<BankAccountTransaction, Long> {
    Page<BankAccountTransaction> findByBankAccountIdOrderByCreatedAtDesc(Long bankAccountId, Pageable pageable);
}
