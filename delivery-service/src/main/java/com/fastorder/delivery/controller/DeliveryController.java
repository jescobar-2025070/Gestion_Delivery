package com.fastorder.delivery.controller;

import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.security.AuthenticatedUser;
import com.fastorder.delivery.domain.dto.ActualizarEstadoRequest;
import com.fastorder.delivery.domain.dto.PedidoDeliveryResponse;
import com.fastorder.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/pedidos")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * Consulta pedidos disponibles para entrega.
     * REPARTIDOR: ve sus pedidos asignados + pedidos sin asignar.
     * ADMIN: ve todos los pedidos activos.
     */
    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('REPARTIDOR') or hasRole('ADMIN')")
    public Page<PedidoDeliveryResponse> getPedidosDisponibles(
            @AuthenticationPrincipal AuthenticatedUser user,
            Pageable pageable) {
        boolean esAdmin = "ADMIN".equals(user.role());
        return deliveryService.getPedidosDisponibles(user.id(), esAdmin, pageable);
    }

    /**
     * Actualiza el estado de un pedido.
     * Emite evento SSE al cambiar.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('REPARTIDOR') or hasRole('ADMIN')")
    public PedidoDeliveryResponse actualizarEstado(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ActualizarEstadoRequest request) {
        boolean esAdmin = "ADMIN".equals(user.role());
        return deliveryService.actualizarEstado(id, user.id(), esAdmin, request.estado());
    }

    /**
     * Suscripción SSE para seguimiento de estado del pedido.
     * CLIENTE: solo puede seguir sus propios pedidos.
     * ADMIN: puede seguir cualquier pedido.
     */
    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public SseEmitter streamEstadoPedido(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        boolean esAdmin = "ADMIN".equals(user.role());
        return deliveryService.suscribirSeguimiento(id, user.id(), esAdmin);
    }
}
