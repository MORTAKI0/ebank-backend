package com.ebank.backend.dashboard.service;

import com.ebank.backend.dashboard.dto.AccountItemDto;
import com.ebank.backend.dashboard.dto.DashboardResponse;
import com.ebank.backend.dashboard.dto.MyAccountsResponse;
import com.ebank.backend.dashboard.dto.TransactionItemDto;
import com.ebank.backend.dashboard.dto.TransactionsPageResponse;
import com.ebank.backend.entity.BankAccount;
import com.ebank.backend.entity.BankAccountTransaction;
import com.ebank.backend.entity.Customer;
import com.ebank.backend.entity.Role;
import com.ebank.backend.entity.User;
import com.ebank.backend.repository.BankAccountRepository;
import com.ebank.backend.repository.BankAccountTransactionRepository;
import com.ebank.backend.repository.CustomerRepository;
import com.ebank.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardService {

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public DashboardService(BankAccountRepository bankAccountRepository,
                            BankAccountTransactionRepository transactionRepository,
                            CustomerRepository customerRepository,
                            UserRepository userRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    public MyAccountsResponse getMyAccounts() {
        Customer customer = getCurrentCustomer();
        List<AccountItemDto> accounts = bankAccountRepository.findAccountItemsByCustomerId(customer.getId());

        Long defaultAccountId = null;
        List<Long> ids = bankAccountRepository.findMostRecentlyMovedAccountIdByCustomerId(
                customer.getId(), PageRequest.of(0, 1));
        if (!ids.isEmpty()) {
            defaultAccountId = ids.get(0);
        }

        return new MyAccountsResponse(defaultAccountId, accounts);
    }

    public DashboardResponse getDashboard(Long accountId) {
        BankAccount account = getOwnedAccount(accountId);

        Page<BankAccountTransaction> page = transactionRepository.findByBankAccountIdOrderByCreatedAtDesc(
                accountId, PageRequest.of(0, 10));
        List<TransactionItemDto> lastTransactions = page.getContent().stream()
                .map(this::toTransactionItem)
                .toList();

        AccountItemDto accountItem = new AccountItemDto(
                account.getId(),
                account.getRib(),
                account.getAmount(),
                account.getAccountStatus(),
                account.getCreatedAt(),
                transactionRepository.findMaxCreatedAtByBankAccountId(accountId).orElse(null)
        );

        return new DashboardResponse(accountItem, lastTransactions);
    }

    public TransactionsPageResponse getTransactions(Long accountId, Pageable pageable) {
        getOwnedAccount(accountId);
        Page<BankAccountTransaction> page = transactionRepository.findByBankAccountIdOrderByCreatedAtDesc(
                accountId, pageable);
        List<TransactionItemDto> content = page.getContent().stream()
                .map(this::toTransactionItem)
                .toList();

        return new TransactionsPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase()));
        if (user.getRole() != Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase());
        }
        if (user instanceof Customer customer) {
            return customer;
        }
        return customerRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));
    }

    private BankAccount getOwnedAccount(Long accountId) {
        Customer customer = getCurrentCustomer();
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte introuvable"));
        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase());
        }
        return account;
    }

    private TransactionItemDto toTransactionItem(BankAccountTransaction transaction) {
        return new TransactionItemDto(
                transaction.getLabel(),
                transaction.getTransactionType(),
                transaction.getCreatedAt(),
                transaction.getAmount()
        );
    }
}
