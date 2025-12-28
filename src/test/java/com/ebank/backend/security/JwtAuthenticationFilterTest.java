package com.ebank.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Test
    void rejectsExpiredTokenWithExpectedMessage() throws Exception {
        when(jwtService.extractUsername("expired-token"))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/me");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            throw new AssertionError("Filter chain should not continue");
        };

        JwtAuthenticationException ex = assertThrows(JwtAuthenticationException.class,
                () -> filter.doFilter(request, response, chain));

        assertEquals("Session invalide, veuillez s\u2019authentifier", ex.getMessage());
    }
}
