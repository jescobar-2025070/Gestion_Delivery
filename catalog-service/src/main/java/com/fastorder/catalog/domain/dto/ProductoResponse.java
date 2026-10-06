package com.fastorder.catalog.domain.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        Long comercioId,
        String nombre,
        BigDecimal precio,
        Integer stock,
        Boolean disponible
) {
}
