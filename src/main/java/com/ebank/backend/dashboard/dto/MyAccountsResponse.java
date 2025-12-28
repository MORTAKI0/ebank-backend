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
public class MyAccountsResponse {

    private Long defaultAccountId;
    private List<AccountItemDto> accounts;
}
