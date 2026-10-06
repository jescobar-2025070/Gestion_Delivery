package com.fastorder.delivery.domain.dto;

import com.fastorder.delivery.domain.enums.EstadoPedido;
import java.math.BigDecimal;
import java.time.Instant;

public record PedidoDeliveryResponse(
        Long id,
        Long clienteId,
        Long repartidorId,
        Instant fechaPedido,
        BigDecimal montoTotal,
        EstadoPedido estado
) {
}
