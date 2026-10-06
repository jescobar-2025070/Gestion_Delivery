package com.fastorder.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración CORS compartida (orígenes explícitos, sin comodines globales). */
@ConfigurationProperties(prefix = "fastorder.cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        boolean allowCredentials,
        long maxAgeSeconds) {
}
