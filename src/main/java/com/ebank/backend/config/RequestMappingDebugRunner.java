package com.ebank.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RequestMappingDebugRunner {

    private static final Logger logger = LoggerFactory.getLogger(RequestMappingDebugRunner.class);

    @Bean
    public ApplicationRunner requestMappingDebug(RequestMappingHandlerMapping mapping) {
        return args -> mapping.getHandlerMethods().forEach((info, method) -> {
            List<String> patterns = new ArrayList<>();
            if (info.getPathPatternsCondition() != null) {
                patterns.addAll(info.getPathPatternsCondition().getPatternValues());
            } else if (info.getPatternsCondition() != null) {
                patterns.addAll(info.getPatternsCondition().getPatterns());
            }

            boolean matches = patterns.stream().anyMatch(pattern -> pattern.contains("/api/me"));
            if (matches) {
                logger.info("Request mapping for /api/me: {} -> {}", patterns, method.toString());
            }
        });
    }
}
