package com.ebank.backend.dashboard.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsPageResponse {

    private List<TransactionItemDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
