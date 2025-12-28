package com.ebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ebank.backend.dto.LoginRequestDto;
import com.ebank.backend.entity.Role;
import com.ebank.backend.entity.User;
import com.ebank.backend.repository.UserRepository;
import com.ebank.backend.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginRejectsUnknownUser() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        LoginRequestDto request = new LoginRequestDto("missing", "pass");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Login ou mot de passe erron\u00e9s", ex.getReason());
    }

    @Test
    void loginRejectsBadPassword() {
        User user = new User();
        user.setUsername("client1");
        user.setPassword("hashed");
        user.setRole(Role.CLIENT);

        when(userRepository.findByUsername("client1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hashed")).thenReturn(false);

        LoginRequestDto request = new LoginRequestDto("client1", "bad");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Login ou mot de passe erron\u00e9s", ex.getReason());
    }
}
