package com.fastorder.order.service;

import com.fastorder.common.exception.InsufficientStockException;
import com.fastorder.common.exception.ResourceNotFoundException;
import com.fastorder.order.domain.dto.DetallePedidoResponse;
import com.fastorder.order.domain.dto.PedidoRequest;
import com.fastorder.order.domain.dto.PedidoResponse;
import com.fastorder.order.domain.entity.DetallePedido;
import com.fastorder.order.domain.entity.Pedido;
import com.fastorder.order.domain.entity.ProductoStock;
import com.fastorder.order.domain.enums.EstadoPedido;
import com.fastorder.order.repository.PedidoRepository;
import com.fastorder.order.repository.ProductoStockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private final PedidoRepository pedidoRepository;
    private final ProductoStockRepository productoStockRepository;
    private static final BigDecimal COSTO_ENVIO = new BigDecimal("20.00");

    public OrderService(PedidoRepository pedidoRepository, ProductoStockRepository productoStockRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoStockRepository = productoStockRepository;
    }

    @Transactional
    public PedidoResponse createPedido(Long clienteId, PedidoRequest request) {
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

    public Page<PedidoResponse> getMisPedidos(Long clienteId, Pageable pageable) {
        return pedidoRepository.findByClienteId(clienteId, pageable)
                .map(this::mapToResponse);
    }

    private PedidoResponse mapToResponse(Pedido p) {
        List<DetallePedidoResponse> detalles = p.getDetalles().stream()
                .map(d -> new DetallePedidoResponse(d.getId(), d.getProductoId(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
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
