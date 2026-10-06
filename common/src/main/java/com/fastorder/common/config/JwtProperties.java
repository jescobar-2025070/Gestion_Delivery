package com.fastorder.common.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración JWT compartida por todos los microservicios.
 * El secreto debe provenir de una variable de entorno (mínimo 32 caracteres para HS256).
 */
@Validated
@ConfigurationProperties(prefix = "fastorder.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32) String secret,
        @Positive long expirationMs) {
}
