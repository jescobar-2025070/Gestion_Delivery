package com.fastorder.order.service;

import com.fastorder.common.exception.InsufficientStockException;
import com.fastorder.common.exception.InvalidStatusException;
import com.fastorder.common.exception.ResourceNotFoundException;
import com.fastorder.order.domain.dto.CambioEstadoRequest;
import com.fastorder.order.domain.dto.DetallePedidoResponse;
import com.fastorder.order.domain.dto.PedidoRequest;
import com.fastorder.order.domain.dto.PedidoResponse;
import com.fastorder.order.domain.entity.DetallePedido;
import com.fastorder.order.domain.entity.Pedido;
import com.fastorder.order.domain.entity.ProductoStock;
import com.fastorder.order.domain.enums.EstadoPedido;
import com.fastorder.order.domain.enums.EstadoTransicion;
import com.fastorder.order.repository.PedidoRepository;
import com.fastorder.order.repository.ProductoStockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private final PedidoRepository pedidoRepository;
    private final ProductoStockRepository productoStockRepository;
    private final com.fastorder.common.audit.AuditService auditService;
    private static final BigDecimal COSTO_ENVIO = new BigDecimal("20.00");

    public OrderService(PedidoRepository pedidoRepository, ProductoStockRepository productoStockRepository, com.fastorder.common.audit.AuditService auditService) {
        this.pedidoRepository = pedidoRepository;
        this.productoStockRepository = productoStockRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PedidoResponse createPedido(Long clienteId, PedidoRequest request) {
        // Ordenar IDs para evitar deadlocks
        List<PedidoRequest.ItemRequest> sortedItems = request.productos().stream()
                .sorted(Comparator.comparing(PedidoRequest.ItemRequest::productoId))
                .toList();

        Pedido pedido = Pedido.builder()
                .clienteId(clienteId)
                .fechaPedido(Instant.now())
                .costoEnvio(COSTO_ENVIO)
                .estado(EstadoPedido.PENDIENTE)
                .build();

        BigDecimal sumaSubtotales = BigDecimal.ZERO;

        for (PedidoRequest.ItemRequest item : sortedItems) {
            ProductoStock stock = productoStockRepository.findByIdForUpdate(item.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.productoId()));

            if (!stock.getDisponible() || stock.getStock() < item.cantidad()) {
                throw new InsufficientStockException("Stock insuficiente para el producto: " + stock.getNombre());
            }

            stock.setStock(stock.getStock() - item.cantidad());
            productoStockRepository.save(stock);

            BigDecimal subtotal = stock.getPrecio().multiply(BigDecimal.valueOf(item.cantidad()));
            sumaSubtotales = sumaSubtotales.add(subtotal);

            DetallePedido detalle = DetallePedido.builder()
                    .productoId(item.productoId())
                    .cantidad(item.cantidad())
                    .precioUnitario(stock.getPrecio())
                    .subtotal(subtotal)
                    .build();

            pedido.addDetalle(detalle);
        }

        pedido.setMontoTotal(sumaSubtotales.add(COSTO_ENVIO));
        pedido = pedidoRepository.save(pedido);

        return mapToResponse(pedido);
    }

    @Transactional
    public Page<PedidoResponse> getMisPedidos(Long clienteId, Pageable pageable) {
        return pedidoRepository.findByClienteId(clienteId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public PedidoResponse cancelarPedido(Long pedidoId, com.fastorder.common.security.AuthenticatedUser user) {
        boolean esAdmin = "ADMIN".equals(user.role());
        Long solicitanteId = user.id();
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        if (!esAdmin && !pedido.getClienteId().equals(solicitanteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para cancelar este pedido");
        }

        EstadoTransicion.validar(pedido.getEstado(), EstadoPedido.CANCELADO);

        for (DetallePedido detalle : pedido.getDetalles()) {
            productoStockRepository.findByIdForUpdate(detalle.getProductoId())
                    .ifPresent(stock -> {
                        stock.setStock(stock.getStock() + detalle.getCantidad());
                        productoStockRepository.save(stock);
                    });
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido = pedidoRepository.save(pedido);
        
        if (esAdmin) {
            auditService.registrar(user.id(), user.email(), "PEDIDO", pedido.getId(), "CANCELAR_PEDIDO", "Pedido " + pedido.getId() + " cancelado por ADMIN");
        }
        
        return mapToResponse(pedido);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long pedidoId, com.fastorder.common.security.AuthenticatedUser user, CambioEstadoRequest request) {
        boolean esAdmin = "ADMIN".equals(user.role());
        Long solicitanteId = user.id();
        EstadoPedido nuevoEstado = request.estado();

        if (nuevoEstado == EstadoPedido.CANCELADO || nuevoEstado == EstadoPedido.PENDIENTE) {
            throw new InvalidStatusException("Use el endpoint de cancelación para cancelar pedidos");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        if (!esAdmin) {
            if (pedido.getRepartidorId() == null || !pedido.getRepartidorId().equals(solicitanteId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No tienes permiso para modificar este pedido");
            }
        }

        EstadoTransicion.validar(pedido.getEstado(), nuevoEstado);

        if (pedido.getRepartidorId() == null && !esAdmin) {
            pedido.setRepartidorId(solicitanteId);
        }

        pedido.setEstado(nuevoEstado);
        pedido = pedidoRepository.save(pedido);
        
        if (esAdmin) {
            auditService.registrar(user.id(), user.email(), "PEDIDO", pedido.getId(), "CAMBIAR_ESTADO", "Estado de pedido " + pedido.getId() + " cambiado a " + nuevoEstado + " por ADMIN");
        }
        
        return mapToResponse(pedido);
    }

    private PedidoResponse mapToResponse(Pedido p) {
        List<DetallePedidoResponse> detalles = p.getDetalles().stream()
                .map(d -> new DetallePedidoResponse(
                        d.getId(), d.getProductoId(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();

        return new PedidoResponse(
                p.getId(),
                p.getClienteId(),
                p.getRepartidorId(),
                p.getFechaPedido(),
                p.getCostoEnvio(),
                p.getMontoTotal(),
                p.getEstado(),
                detalles
        );
    }
}
