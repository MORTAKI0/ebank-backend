package com.ebank.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customers_identity_ref", columnNames = "identity_ref"),
                @UniqueConstraint(name = "uk_customers_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_customers_identity_ref", columnList = "identity_ref"),
                @Index(name = "idx_customers_email", columnList = "email")
        }
)
@PrimaryKeyJoinColumn(name = "id")
public class Customer extends User {

    // RG_4: unique identity
    @Column(name = "identity_ref", nullable = false, length = 50)
    private String identityRef;

    // RG_6: unique email
    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    // RG_5: required
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    // RG_5: required
    @Column(name = "postal_address", nullable = false, length = 255)
    private String postalAddress;

    @JsonIgnore
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BankAccount> bankAccounts = new ArrayList<>();
}
