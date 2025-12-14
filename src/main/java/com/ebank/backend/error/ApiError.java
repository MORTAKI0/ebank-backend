package com.ebank.backend.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}









