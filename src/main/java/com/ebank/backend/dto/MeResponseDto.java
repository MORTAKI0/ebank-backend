package com.ebank.backend.dto;

import com.ebank.backend.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeResponseDto {

    @NotNull
    private String username;

    @NotNull
    private Role role;
}
