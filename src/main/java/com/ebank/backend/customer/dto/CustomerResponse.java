package com.ebank.backend.customer.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String identityRef;
    private String email;
    private LocalDate birthDate;
    private String postalAddress;
}
