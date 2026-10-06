package com.fastorder.order.domain.dto;

import com.fastorder.order.domain.enums.EstadoPedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long clienteId,
        Long repartidorId,
        Instant fechaPedido,
        BigDecimal costoEnvio,
        BigDecimal montoTotal,
        EstadoPedido estado,
        List<DetallePedidoResponse> detalles
) {
}
