package com.ebank.backend.common.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int DEFAULT_LENGTH = 12;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder builder = new StringBuilder(DEFAULT_LENGTH);
        for (int i = 0; i < DEFAULT_LENGTH; i++) {
            int idx = secureRandom.nextInt(CHARS.length());
            builder.append(CHARS.charAt(idx));
        }
        return builder.toString();
    }
}
