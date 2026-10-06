package com.fastorder.order.domain.dto;

import com.fastorder.order.domain.enums.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(
        @NotNull EstadoPedido estado
) {
}
