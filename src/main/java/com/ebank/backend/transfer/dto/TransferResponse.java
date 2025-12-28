package com.ebank.backend.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private Long transferId;
    private BigDecimal newBalance;
    private Instant timestamp;
    private Long fromAccountId;
    private String toRib;
    private BigDecimal amount;
}
