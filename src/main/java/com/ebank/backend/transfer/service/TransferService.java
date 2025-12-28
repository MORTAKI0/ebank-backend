package com.ebank.backend.transfer.service;

import com.ebank.backend.common.util.RibValidator;
import com.ebank.backend.entity.AccountStatus;
import com.ebank.backend.entity.BankAccount;
import com.ebank.backend.entity.BankAccountTransaction;
import com.ebank.backend.entity.Customer;
import com.ebank.backend.entity.Role;
import com.ebank.backend.entity.TransactionType;
import com.ebank.backend.entity.User;
import com.ebank.backend.repository.BankAccountRepository;
import com.ebank.backend.repository.BankAccountTransactionRepository;
import com.ebank.backend.repository.CustomerRepository;
import com.ebank.backend.repository.UserRepository;
import com.ebank.backend.transfer.dto.CreateTransferRequest;
import com.ebank.backend.transfer.dto.TransferResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransferService {

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RibValidator ribValidator;

    public TransferService(BankAccountRepository bankAccountRepository,
                           BankAccountTransactionRepository transactionRepository,
                           CustomerRepository customerRepository,
                           UserRepository userRepository,
                           RibValidator ribValidator) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.ribValidator = ribValidator;
    }

    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request, Authentication authentication) {
        Customer customer = getCurrentCustomer(authentication);
        String normalizedToRib = ribValidator.validateOrThrow(request.getToRib());

        BankAccount source = bankAccountRepository.findByIdForUpdate(request.getFromAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte source introuvable"));
        if (!source.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase());
        }
        assertAccountActive(source, "source");

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Montant invalide");
        }
        if (source.getAmount().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solde insuffisant");
        }

        BankAccount destination = bankAccountRepository.findByRibForUpdate(normalizedToRib)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte destinataire introuvable"));
        assertAccountActive(destination, "destination");

        Instant timestamp = Instant.now();
        LocalDateTime createdAt = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());

        source.setAmount(source.getAmount().subtract(amount));
        destination.setAmount(destination.getAmount().add(amount));

        bankAccountRepository.save(source);
        if (!source.getId().equals(destination.getId())) {
            bankAccountRepository.save(destination);
        }

        String motif = request.getMotif();
        BankAccountTransaction debit = buildTransaction(
                TransactionType.DEBIT,
                amount,
                "Virement vers " + normalizedToRib,
                motif,
                createdAt,
                source,
                customer
        );
        BankAccountTransaction credit = buildTransaction(
                TransactionType.CREDIT,
                amount,
                "Virement en votre faveur de " + source.getRib(),
                motif,
                createdAt,
                destination,
                customer
        );

        BankAccountTransaction savedDebit = transactionRepository.save(debit);
        transactionRepository.save(credit);

        return new TransferResponse(
                savedDebit.getId(),
                source.getAmount(),
                timestamp,
                source.getId(),
                normalizedToRib,
                amount
        );
    }

    private Customer getCurrentCustomer(Authentication authentication) {
        Authentication auth = authentication;
        if (auth == null) {
            auth = SecurityContextHolder.getContext().getAuthentication();
        }
        if (auth == null
                || auth instanceof AnonymousAuthenticationToken
                || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        String username = auth.getName();
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

    private void assertAccountActive(BankAccount account, String label) {
        if (account.getAccountStatus() == AccountStatus.BLOCKED
                || account.getAccountStatus() == AccountStatus.CLOSED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Compte " + label + " bloque ou ferme");
        }
    }

    private BankAccountTransaction buildTransaction(TransactionType type,
                                                    BigDecimal amount,
                                                    String label,
                                                    String motif,
                                                    LocalDateTime createdAt,
                                                    BankAccount account,
                                                    User user) {
        BankAccountTransaction transaction = new BankAccountTransaction();
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setLabel(label);
        transaction.setMotif(motif);
        transaction.setCreatedAt(createdAt);
        transaction.setBankAccount(account);
        transaction.setUser(user);
        return transaction;
    }
}
