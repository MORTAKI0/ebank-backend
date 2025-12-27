package com.ebank.backend.account.dto;

import com.ebank.backend.entity.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryDto {

    private Long id;
    private String rib;
    private BigDecimal amount;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private Long customerId;
    private String customerIdentityRef;
    private String customerName;
}
