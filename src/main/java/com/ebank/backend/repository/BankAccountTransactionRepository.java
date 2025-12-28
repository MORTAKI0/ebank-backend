package com.ebank.backend.repository;

import com.ebank.backend.entity.BankAccountTransaction;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BankAccountTransactionRepository extends JpaRepository<BankAccountTransaction, Long> {
    Page<BankAccountTransaction> findByBankAccountIdOrderByCreatedAtDesc(Long bankAccountId, Pageable pageable);

    @Query("select max(t.createdAt) from BankAccountTransaction t where t.bankAccount.id = :accountId")
    Optional<LocalDateTime> findMaxCreatedAtByBankAccountId(@Param("accountId") Long accountId);
}
