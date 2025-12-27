package com.ebank.backend.common.error;

import com.ebank.backend.error.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class SecurityAccessDeniedHandler implements AccessDeniedHandler {

    private static final String FORBIDDEN_MESSAGE =
            "Vous n\u2019avez pas le droit d\u2019acc\u00e9der \u00e0 cette fonctionnalit\u00e9. Veuillez contacter votre administrateur";

    private final ObjectMapper mapper;

    public SecurityAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(HttpServletResponse.SC_FORBIDDEN)
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(FORBIDDEN_MESSAGE)
                .path(request.getRequestURI())
                .build();

        mapper.writeValue(response.getOutputStream(), body);
    }
}
