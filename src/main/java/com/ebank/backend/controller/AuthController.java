package com.ebank.backend.controller;

import com.ebank.backend.auth.dto.ChangePasswordRequestDto;
import com.ebank.backend.dto.AuthResponseDto;
import com.ebank.backend.dto.LoginRequestDto;
import com.ebank.backend.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        authService.changePassword(request);
        return Map.of("message", "Mot de passe modifi\u00e9 avec succ\u00e8s");
    }
}
