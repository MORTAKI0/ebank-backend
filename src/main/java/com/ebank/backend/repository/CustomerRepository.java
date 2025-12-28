package com.ebank.backend.repository;

import com.ebank.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByIdentityRef(String identityRef);
    boolean existsByEmail(String email);
    Optional<Customer> findByIdentityRef(String identityRef);
    Optional<Customer> findByUsername(String username);


}
