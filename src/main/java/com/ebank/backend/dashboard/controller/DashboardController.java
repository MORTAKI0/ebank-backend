package com.ebank.backend.dashboard.controller;

import com.ebank.backend.dashboard.dto.DashboardResponse;
import com.ebank.backend.dashboard.dto.MyAccountsResponse;
import com.ebank.backend.dashboard.dto.TransactionsPageResponse;
import com.ebank.backend.dashboard.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/me/accounts")
    public ResponseEntity<MyAccountsResponse> getMyAccounts() {
        return ResponseEntity.ok(dashboardService.getMyAccounts());
    }

    @GetMapping("/api/accounts/{id}/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.getDashboard(id));
    }

    @GetMapping("/api/accounts/{id}/transactions")
    public ResponseEntity<TransactionsPageResponse> getTransactions(@PathVariable Long id,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(dashboardService.getTransactions(id, pageable));
    }
}
