package com.ebank.backend.transfer.controller;

import com.ebank.backend.transfer.dto.CreateTransferRequest;
import com.ebank.backend.transfer.dto.TransferResponse;
import com.ebank.backend.transfer.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<TransferResponse> createTransfer(@Valid @RequestBody CreateTransferRequest request,
                                                           Authentication authentication) {
        TransferResponse response = transferService.createTransfer(request, authentication);
        return ResponseEntity.ok(response);
    }
}
