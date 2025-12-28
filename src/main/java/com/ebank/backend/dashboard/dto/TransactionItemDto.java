package com.ebank.backend.dashboard.dto;

import com.ebank.backend.entity.TransactionType;
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
public class TransactionItemDto {

    private String label;
    private TransactionType transactionType;
    private LocalDateTime createdAt;
    private BigDecimal amount;
}
