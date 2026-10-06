package com.fastorder.order.domain.dto;

import java.math.BigDecimal;

public record DetallePedidoResponse(
        Long id,
        Long productoId,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
