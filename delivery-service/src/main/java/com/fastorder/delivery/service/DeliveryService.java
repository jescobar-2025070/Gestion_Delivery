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
        if (!esAdmin && pedido.getRepartidorId() != null
                && !pedido.getRepartidorId().equals(solicitanteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar este pedido");
        }

        PedidoDelivery pedido = pedidoDeliveryRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // REPARTIDOR solo puede modificar pedidos asignados a él
        if (!esAdmin) {
            if (pedido.getRepartidorId() == null || !pedido.getRepartidorId().equals(solicitanteId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No tienes permiso para modificar este pedido");
            }
        }

        validarTransicion(pedido.getEstado(), nuevoEstado);

        // Auto-asignación si repartidor toma pedido sin asignar
        if (pedido.getRepartidorId() == null && !esAdmin) {
            pedido.setRepartidorId(solicitanteId);
        }

        pedido.setEstado(nuevoEstado);
        pedido = pedidoDeliveryRepository.save(pedido);

        // Emitir evento SSE
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
