package com.ebank.backend.service;

import com.ebank.backend.dto.MeResponseDto;
import com.ebank.backend.entity.User;
import com.ebank.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MeService {

    private final UserRepository userRepository;

    public MeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public MeResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase()));

        return new MeResponseDto(user.getUsername(), user.getRole());
    }
}
