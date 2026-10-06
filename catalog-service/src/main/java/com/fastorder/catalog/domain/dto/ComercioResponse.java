package com.fastorder.catalog.domain.dto;

import com.fastorder.catalog.domain.enums.Categoria;

public record ComercioResponse(
        Long id,
        String nombre,
        Categoria categoria,
        String direccion,
        Boolean abierto
) {
}
