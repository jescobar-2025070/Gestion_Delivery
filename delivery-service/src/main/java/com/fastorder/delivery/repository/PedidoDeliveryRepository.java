package com.fastorder.delivery.repository;

import com.fastorder.delivery.domain.entity.PedidoDelivery;
import com.fastorder.delivery.domain.enums.EstadoPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoDeliveryRepository extends JpaRepository<PedidoDelivery, Long> {

    /**
     * Pedidos disponibles: sin asignar (PENDIENTE) o asignados al repartidor autenticado.
     */
    @Query("""
            SELECT p FROM PedidoDelivery p
            WHERE p.estado IN ('PENDIENTE', 'EN_PREPARACION', 'EN_CAMINO')
              AND (p.repartidorId IS NULL OR p.repartidorId = :repartidorId)
            """)
    Page<PedidoDelivery> findDisponiblesParaRepartidor(@Param("repartidorId") Long repartidorId, Pageable pageable);

    /**
     * Para ADMIN: pedidos activos (no entregados ni cancelados).
     */
    @Query("""
            SELECT p FROM PedidoDelivery p
            WHERE p.estado IN ('PENDIENTE', 'EN_PREPARACION', 'EN_CAMINO')
            """)
    Page<PedidoDelivery> findPedidosActivos(Pageable pageable);
}
