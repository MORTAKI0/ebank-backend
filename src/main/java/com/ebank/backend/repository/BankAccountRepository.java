package com.ebank.backend.repository;

import com.ebank.backend.dashboard.dto.AccountItemDto;
import com.ebank.backend.entity.BankAccount;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByRib(String rib);
    boolean existsByRib(String rib);
    List<BankAccount> findByCustomerId(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BankAccount b where b.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BankAccount b where b.rib = :rib")
    Optional<BankAccount> findByRibForUpdate(@Param("rib") String rib);

    @Query("""
            select new com.ebank.backend.dashboard.dto.AccountItemDto(
                b.id, b.rib, b.amount, b.accountStatus, b.createdAt, max(t.createdAt)
            )
            from BankAccount b
            left join BankAccountTransaction t on t.bankAccount = b
            where b.customer.id = :customerId
            group by b.id, b.rib, b.amount, b.accountStatus, b.createdAt
            order by b.createdAt desc
            """)
    List<AccountItemDto> findAccountItemsByCustomerId(@Param("customerId") Long customerId);

    @Query("""
            select b.id
            from BankAccount b
            left join BankAccountTransaction t on t.bankAccount = b
            where b.customer.id = :customerId
            group by b.id, b.createdAt
            order by coalesce(max(t.createdAt), b.createdAt) desc
            """)
    List<Long> findMostRecentlyMovedAccountIdByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
}
