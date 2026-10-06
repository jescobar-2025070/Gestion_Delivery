package com.fastorder.delivery.domain.entity;

import com.fastorder.delivery.domain.enums.EstadoPedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Vista de solo lectura / actualización del pedido en delivery-service.
 * Mapea la misma tabla "pedidos" gestionada por order-service.
 * Solo se accede a los campos necesarios para el flujo de entrega.
 */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
public class PedidoDelivery {

    @Id
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "repartidor_id")
    private Long repartidorId;

    @Column(nullable = false)
    private Instant fechaPedido;

    @Column(nullable = false)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;
}
