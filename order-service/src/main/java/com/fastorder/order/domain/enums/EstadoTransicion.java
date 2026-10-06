package com.fastorder.order.domain.enums;

import com.fastorder.common.exception.InvalidStatusException;

import java.util.Set;
import java.util.Map;

/**
 * Validador de transiciones de estado del pedido.
 * Define el grafo dirigido de transiciones permitidas.
 */
public final class EstadoTransicion {

    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES_VALIDAS = Map.of(
            EstadoPedido.PENDIENTE,       Set.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO),
            EstadoPedido.EN_PREPARACION,  Set.of(EstadoPedido.EN_CAMINO),
            EstadoPedido.EN_CAMINO,       Set.of(EstadoPedido.ENTREGADO),
            EstadoPedido.ENTREGADO,       Set.of(),
            EstadoPedido.CANCELADO,       Set.of()
    );

    private EstadoTransicion() {
    }

    public static void validar(EstadoPedido actual, EstadoPedido nuevo) {
        Set<EstadoPedido> permitidos = TRANSICIONES_VALIDAS.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new InvalidStatusException(
                    "Transición inválida: " + actual + " → " + nuevo);
        }
    }
}
