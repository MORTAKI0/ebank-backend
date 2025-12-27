package com.ebank.backend.common.util;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RibValidator {

    public String validateOrThrow(String rib) {
        String normalized = normalize(rib);
        if (normalized.length() < 10 || normalized.length() > 34) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RIB invalide");
        }
        if (looksLikeIban(normalized) && !isValidIban(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RIB invalide");
        }
        return normalized;
    }

    private String normalize(String rib) {
        if (rib == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RIB invalide");
        }
        String trimmed = rib.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RIB invalide");
        }
        if (!trimmed.matches("^[A-Za-z0-9 ]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RIB invalide");
        }
        return trimmed.replace(" ", "").toUpperCase(Locale.ROOT);
    }

    private boolean looksLikeIban(String rib) {
        return rib.startsWith("MA") || rib.matches("^[A-Z]{2}\\d{2}.*$");
    }

    private boolean isValidIban(String rib) {
        if (rib.length() < 4) {
            return false;
        }
        String rearranged = rib.substring(4) + rib.substring(0, 4);
        int mod = 0;
        for (int i = 0; i < rearranged.length(); i++) {
            char c = rearranged.charAt(i);
            if (c >= '0' && c <= '9') {
                mod = (mod * 10 + (c - '0')) % 97;
            } else if (c >= 'A' && c <= 'Z') {
                int val = c - 'A' + 10;
                mod = (mod * 100 + val) % 97;
            } else {
                return false;
            }
        }
        return mod == 1;
    }
}
