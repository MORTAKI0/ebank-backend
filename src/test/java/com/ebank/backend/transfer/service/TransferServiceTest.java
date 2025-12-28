package com.ebank.backend.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ebank.backend.common.util.RibValidator;
import com.ebank.backend.entity.AccountStatus;
import com.ebank.backend.entity.BankAccount;
import com.ebank.backend.entity.BankAccountTransaction;
import com.ebank.backend.entity.Customer;
import com.ebank.backend.entity.Role;
import com.ebank.backend.entity.TransactionType;
import com.ebank.backend.repository.BankAccountRepository;
import com.ebank.backend.repository.BankAccountTransactionRepository;
import com.ebank.backend.repository.CustomerRepository;
import com.ebank.backend.repository.UserRepository;
import com.ebank.backend.transfer.dto.CreateTransferRequest;
import com.ebank.backend.transfer.dto.TransferResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountTransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RibValidator ribValidator;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransferService transferService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(10L);
        customer.setRole(Role.CLIENT);
        customer.setUsername("client1");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("client1");
        when(userRepository.findByUsername("client1")).thenReturn(Optional.of(customer));
        when(ribValidator.validateOrThrow(anyString())).thenReturn("RIBDEST");
    }

    @Test
    void createTransferRejectsBlockedSource() {
        CreateTransferRequest request = new CreateTransferRequest(
                1L,
                new BigDecimal("10.00"),
                "RIBDEST",
                null
        );

        BankAccount source = new BankAccount();
        source.setId(1L);
        source.setRib("RIBSOURCE");
        source.setAmount(new BigDecimal("100.00"));
        source.setAccountStatus(AccountStatus.BLOCKED);
        source.setCustomer(customer);

        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> transferService.createTransfer(request, authentication));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(bankAccountRepository, never()).findByRibForUpdate(anyString());
    }

    @Test
    void createTransferRejectsInsufficientFunds() {
        CreateTransferRequest request = new CreateTransferRequest(
                1L,
                new BigDecimal("10.00"),
                "RIBDEST",
                null
        );

        BankAccount source = new BankAccount();
        source.setId(1L);
        source.setRib("RIBSOURCE");
        source.setAmount(new BigDecimal("5.00"));
        source.setAccountStatus(AccountStatus.OPEN);
        source.setCustomer(customer);

        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> transferService.createTransfer(request, authentication));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(bankAccountRepository, never()).findByRibForUpdate(anyString());
    }

    @Test
    void createTransferSucceedsWithDebitAndCredit() {
        CreateTransferRequest request = new CreateTransferRequest(
                1L,
                new BigDecimal("10.00"),
                "RIBDEST",
                "rent"
        );

        BankAccount source = new BankAccount();
        source.setId(1L);
        source.setRib("RIBSOURCE");
        source.setAmount(new BigDecimal("100.00"));
        source.setAccountStatus(AccountStatus.OPEN);
        source.setCustomer(customer);

        BankAccount destination = new BankAccount();
        destination.setId(2L);
        destination.setRib("RIBDEST");
        destination.setAmount(new BigDecimal("20.00"));
        destination.setAccountStatus(AccountStatus.OPEN);
        destination.setCustomer(customer);

        when(bankAccountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(bankAccountRepository.findByRibForUpdate("RIBDEST")).thenReturn(Optional.of(destination));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(BankAccountTransaction.class))).thenAnswer(invocation -> {
            BankAccountTransaction tx = invocation.getArgument(0);
            if (tx.getTransactionType() == TransactionType.DEBIT) {
                tx.setId(1L);
            } else {
                tx.setId(2L);
            }
            return tx;
        });

        TransferResponse response = transferService.createTransfer(request, authentication);

        assertEquals(1L, response.getTransferId());
        assertEquals(0, response.getNewBalance().compareTo(new BigDecimal("90.00")));
        assertEquals(1L, response.getFromAccountId());
        assertEquals("RIBDEST", response.getToRib());
        assertEquals(0, response.getAmount().compareTo(new BigDecimal("10.00")));
        assertNotNull(response.getTimestamp());

        assertEquals(0, source.getAmount().compareTo(new BigDecimal("90.00")));
        assertEquals(0, destination.getAmount().compareTo(new BigDecimal("30.00")));

        ArgumentCaptor<BankAccountTransaction> captor = ArgumentCaptor.forClass(BankAccountTransaction.class);
        verify(transactionRepository, times(2)).save(captor.capture());
        List<BankAccountTransaction> saved = captor.getAllValues();

        BankAccountTransaction debit = saved.stream()
                .filter(tx -> tx.getTransactionType() == TransactionType.DEBIT)
                .findFirst()
                .orElseThrow();
        BankAccountTransaction credit = saved.stream()
                .filter(tx -> tx.getTransactionType() == TransactionType.CREDIT)
                .findFirst()
                .orElseThrow();

        assertEquals("Virement vers RIBDEST", debit.getLabel());
        assertEquals("Virement en votre faveur de RIBSOURCE", credit.getLabel());
        assertEquals(debit.getCreatedAt(), credit.getCreatedAt());
        assertEquals("rent", debit.getMotif());
        assertEquals("rent", credit.getMotif());
    }
}
