package com.ebank.backend.transfer.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransferRequest {

    @NotNull
    private Long fromAccountId;

    @NotNull
    @Positive
    @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;

    @NotBlank
    private String toRib;

    @Size(max = 255)
    private String motif;
}
