package com.fastorder.catalog.domain.dto;

import com.fastorder.catalog.domain.enums.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComercioRequest(
        @NotBlank String nombre,
        @NotNull Categoria categoria,
        @NotBlank String direccion,
        @NotNull Boolean abierto
) {
}
