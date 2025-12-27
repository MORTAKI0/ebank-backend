package com.ebank.backend.customer.service;

import com.ebank.backend.common.util.LoginGenerator;
import com.ebank.backend.common.util.PasswordGenerator;
import com.ebank.backend.customer.dto.CreateCustomerRequest;
import com.ebank.backend.customer.dto.CustomerResponse;
import com.ebank.backend.entity.Customer;
import com.ebank.backend.entity.Role;
import com.ebank.backend.mail.MailService;
import com.ebank.backend.repository.CustomerRepository;
import com.ebank.backend.repository.UserRepository;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginGenerator loginGenerator;
    private final PasswordGenerator passwordGenerator;
    private final MailService mailService;

    public CustomerService(CustomerRepository customerRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           LoginGenerator loginGenerator,
                           PasswordGenerator passwordGenerator,
                           MailService mailService) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginGenerator = loginGenerator;
        this.passwordGenerator = passwordGenerator;
        this.mailService = mailService;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByIdentityRef(request.getIdentityRef())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "identityRef d\u00e9j\u00e0 utilis\u00e9");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email d\u00e9j\u00e0 utilis\u00e9");
        }

        String baseUsername = loginGenerator.generateBase(request.getFirstName(), request.getLastName());
        String username = ensureUniqueUsername(baseUsername);
        String rawPassword = passwordGenerator.generate();

        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(passwordEncoder.encode(rawPassword));
        customer.setRole(Role.CLIENT);
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setIdentityRef(request.getIdentityRef());
        customer.setBirthDate(request.getBirthDate());
        customer.setEmail(request.getEmail());
        customer.setPostalAddress(request.getPostalAddress());

        Customer saved;
        try {
            saved = customerRepository.save(customer);
        } catch (DataIntegrityViolationException ex) {
            throw mapConflict(ex);
        }

        mailService.sendNewCustomerCredentials(saved.getEmail(), saved.getUsername(), rawPassword);
        return new CustomerResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getIdentityRef(),
                saved.getEmail(),
                saved.getBirthDate(),
                saved.getPostalAddress()
        );
    }

    private String ensureUniqueUsername(String baseUsername) {
        String candidate = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = baseUsername + suffix;
            suffix++;
        }
        return candidate;
    }

    private ResponseStatusException mapConflict(DataIntegrityViolationException ex) {
        String rawMessage = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        String lower = rawMessage == null ? "" : rawMessage.toLowerCase(Locale.ROOT);
        if (lower.contains("uk_customers_identity_ref") || lower.contains("identity_ref")) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "identityRef d\u00e9j\u00e0 utilis\u00e9");
        }
        if (lower.contains("uk_customers_email") || lower.contains("email")) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "Email d\u00e9j\u00e0 utilis\u00e9");
        }
        return new ResponseStatusException(HttpStatus.CONFLICT, "Conflit de donn\u00e9es");
    }
}
