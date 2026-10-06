package com.fastorder.delivery.domain.dto;

import com.fastorder.delivery.domain.enums.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoRequest(
        @NotNull EstadoPedido estado
) {
}
