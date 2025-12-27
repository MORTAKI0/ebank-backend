package com.ebank.backend.account.service;

import com.ebank.backend.account.dto.AccountSummaryDto;
import com.ebank.backend.account.dto.CreateAccountRequest;
import com.ebank.backend.common.util.RibValidator;
import com.ebank.backend.entity.AccountStatus;
import com.ebank.backend.entity.BankAccount;
import com.ebank.backend.entity.Customer;
import com.ebank.backend.repository.BankAccountRepository;
import com.ebank.backend.repository.CustomerRepository;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerRepository customerRepository;
    private final RibValidator ribValidator;

    public AccountService(BankAccountRepository bankAccountRepository,
                          CustomerRepository customerRepository,
                          RibValidator ribValidator) {
        this.bankAccountRepository = bankAccountRepository;
        this.customerRepository = customerRepository;
        this.ribValidator = ribValidator;
    }

    public AccountSummaryDto createAccount(CreateAccountRequest request) {
        Customer customer = customerRepository.findByIdentityRef(request.getIdentityRef())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));

        String normalizedRib = ribValidator.validateOrThrow(request.getRib());
        if (bankAccountRepository.existsByRib(normalizedRib)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RIB d\u00e9j\u00e0 utilis\u00e9");
        }

        BankAccount account = new BankAccount();
        account.setRib(normalizedRib);
        account.setAmount(BigDecimal.ZERO);
        account.setAccountStatus(AccountStatus.OPEN);
        account.setCustomer(customer);

        BankAccount saved;
        try {
            saved = bankAccountRepository.save(account);
        } catch (DataIntegrityViolationException ex) {
            throw mapConflict(ex);
        }

        String customerName = saved.getCustomer().getFirstName() + " " + saved.getCustomer().getLastName();
        return new AccountSummaryDto(
                saved.getId(),
                saved.getRib(),
                saved.getAmount(),
                saved.getAccountStatus(),
                saved.getCreatedAt(),
                saved.getCustomer().getId(),
                saved.getCustomer().getIdentityRef(),
                customerName
        );
    }

    private ResponseStatusException mapConflict(DataIntegrityViolationException ex) {
        String rawMessage = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        String lower = rawMessage == null ? "" : rawMessage.toLowerCase(Locale.ROOT);
        if (lower.contains("uk_bank_accounts_rib") || lower.contains("rib")) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "RIB d\u00e9j\u00e0 utilis\u00e9");
        }
        return new ResponseStatusException(HttpStatus.CONFLICT, "Conflit de donn\u00e9es");
    }
}
