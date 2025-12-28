package com.ebank.backend.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String FORBIDDEN_MESSAGE =
            "Vous n\u2019avez pas le droit d\u2019acc\u00e9der \u00e0 cette fonctionnalit\u00e9. Veuillez contacter votre administrateur";
    private static final String INVALID_SESSION_MESSAGE = "Session invalide, veuillez s\u2019authentifier";
    private static final String INVALID_REQUEST_MESSAGE = "Requete invalide";

    // 400 - DTO validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // Handles ResponseStatusException you throw in services/controllers
    // Example: throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou mot de passe erron\u00e9s");
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex,
                                                         HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return build(status, message, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                       HttpServletRequest request) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = FORBIDDEN_MESSAGE;
        }
        return build(HttpStatus.FORBIDDEN, message, request);
    }

    // 401 - Spring Security auth failures (bad credentials, invalid token, etc.)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex,
                                                         HttpServletRequest request) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = INVALID_SESSION_MESSAGE;
        }
        return build(HttpStatus.UNAUTHORIZED, message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          HttpServletRequest request) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = INVALID_REQUEST_MESSAGE;
        }
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // 404 - missing route/static resource
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), request);
    }

    // 500 - fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOtherExceptions(Exception ex,
                                                          HttpServletRequest request) {
        if (ex instanceof NoResourceFoundException) {
            return build(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), request);
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(error);
    }
}
