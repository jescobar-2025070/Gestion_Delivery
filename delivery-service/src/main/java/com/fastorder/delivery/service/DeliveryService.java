package com.fastorder.delivery.service;

import com.fastorder.common.exception.InvalidStatusException;
import com.fastorder.common.exception.ResourceNotFoundException;
import com.fastorder.delivery.domain.dto.PedidoDeliveryResponse;
import com.fastorder.delivery.domain.dto.SseEventDto;
import com.fastorder.delivery.domain.entity.PedidoDelivery;
import com.fastorder.delivery.domain.enums.EstadoPedido;
import com.fastorder.delivery.repository.PedidoDeliveryRepository;
import com.fastorder.delivery.sse.SseEmitterManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Service
public class DeliveryService {

    /** Grafo de transiciones válidas (duplicado solo para validación en delivery-service). */
    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES = Map.of(
            EstadoPedido.PENDIENTE,      Set.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO),
            EstadoPedido.EN_PREPARACION, Set.of(EstadoPedido.EN_CAMINO),
            EstadoPedido.EN_CAMINO,      Set.of(EstadoPedido.ENTREGADO),
            EstadoPedido.ENTREGADO,      Set.of(),
            EstadoPedido.CANCELADO,      Set.of()
    );

    private final PedidoDeliveryRepository pedidoDeliveryRepository;
    private final SseEmitterManager sseEmitterManager;

    public DeliveryService(PedidoDeliveryRepository pedidoDeliveryRepository,
                           SseEmitterManager sseEmitterManager) {
        this.pedidoDeliveryRepository = pedidoDeliveryRepository;
        this.sseEmitterManager = sseEmitterManager;
    }

    public Page<PedidoDeliveryResponse> getPedidosDisponibles(Long solicitanteId, boolean esAdmin, Pageable pageable) {
        if (esAdmin) {
            return pedidoDeliveryRepository.findPedidosActivos(pageable).map(this::mapToResponse);
        }
        return pedidoDeliveryRepository.findDisponiblesParaRepartidor(solicitanteId, pageable).map(this::mapToResponse);
    }

    @Transactional
    public PedidoDeliveryResponse actualizarEstado(Long pedidoId, Long solicitanteId, boolean esAdmin, EstadoPedido nuevoEstado) {

        // 1. Primero obtenemos el pedido de la base de datos
        PedidoDelivery pedido = pedidoDeliveryRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // 2. Validamos los permisos del repartidor
        if (!esAdmin) {
            // Si el pedido YA tiene un repartidor asignado, verificamos que sea el mismo que hace la solicitud
            if (pedido.getRepartidorId() != null && !pedido.getRepartidorId().equals(solicitanteId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No tienes permiso para modificar este pedido");
            }
            // Si getRepartidorId() es null, permitimos que continúe para auto-asignarse más adelante
        }

        // 3. Validamos que la transición de estado sea correcta
        validarTransicion(pedido.getEstado(), nuevoEstado);

        // 4. Auto-asignación si un repartidor toma un pedido que aún no tiene repartidor
        if (!esAdmin && pedido.getRepartidorId() == null) {
            pedido.setRepartidorId(solicitanteId);
        }

        // 5. Aplicamos los cambios y guardamos
        pedido.setEstado(nuevoEstado);
        pedido = pedidoDeliveryRepository.save(pedido);

        // 6. Emitimos el evento SSE
        sseEmitterManager.emit(pedidoId, new SseEventDto(pedidoId, nuevoEstado, Instant.now()));

        return mapToResponse(pedido);
    }

    public SseEmitter suscribirSeguimiento(Long pedidoId, Long clienteId, boolean esAdmin) {
        PedidoDelivery pedido = pedidoDeliveryRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // CLIENTE solo puede suscribirse a sus propios pedidos
        if (!esAdmin && !pedido.getClienteId().equals(clienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para seguir este pedido");
        }

        return sseEmitterManager.subscribe(pedidoId);
    }

    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        if (!TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo)) {
            throw new InvalidStatusException("Transición inválida: " + actual + " → " + nuevo);
        }
    }

    private PedidoDeliveryResponse mapToResponse(PedidoDelivery p) {
        return new PedidoDeliveryResponse(
                p.getId(),
                p.getClienteId(),
                p.getRepartidorId(),
                p.getFechaPedido(),
                p.getMontoTotal(),
                p.getEstado()
        );
    }
}