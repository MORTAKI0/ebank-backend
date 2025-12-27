package com.ebank.backend.common.util;

import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class LoginGenerator {

    public String generateBase(String firstName, String lastName) {
        String raw = (safe(firstName) + "." + safe(lastName)).toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String cleaned = normalized.replaceAll("[^a-z0-9.]", "");
        if (cleaned.isBlank()) {
            return "user";
        }
        return cleaned;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
