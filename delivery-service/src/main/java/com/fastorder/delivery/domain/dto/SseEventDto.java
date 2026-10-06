package com.fastorder.delivery.domain.dto;

import com.fastorder.delivery.domain.enums.EstadoPedido;
import java.time.Instant;

public record SseEventDto(
        Long pedidoId,
        EstadoPedido estado,
        Instant timestamp
) {
}
