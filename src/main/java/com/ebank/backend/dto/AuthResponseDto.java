package com.ebank.backend.dto;

import com.ebank.backend.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    @NotNull
    private String token;

    @NotNull
    private Role role;
}
