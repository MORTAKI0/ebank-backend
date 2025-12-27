package com.ebank.backend.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank
    @Size(min = 10, max = 34)
    @Pattern(regexp = "^[A-Za-z0-9 ]+$")
    private String rib;

    @NotBlank
    private String identityRef;
}
