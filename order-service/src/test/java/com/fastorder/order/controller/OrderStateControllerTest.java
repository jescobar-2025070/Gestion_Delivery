package com.fastorder.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.security.JwtService;
import com.fastorder.order.domain.dto.CambioEstadoRequest;
import com.fastorder.order.domain.dto.PedidoRequest;
import com.fastorder.order.domain.entity.DetallePedido;
import com.fastorder.order.domain.entity.Pedido;
import com.fastorder.order.domain.entity.ProductoStock;
import com.fastorder.order.domain.enums.EstadoPedido;
import com.fastorder.order.repository.PedidoRepository;
import com.fastorder.order.repository.ProductoStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoStockRepository productoStockRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Pedido pedidoPendiente;
    private ProductoStock testProduct;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        productoStockRepository.deleteAll();

        testProduct = new ProductoStock();
        testProduct.setId(20L);
        testProduct.setNombre("Producto Estado");
        testProduct.setPrecio(new BigDecimal("10.00"));
        testProduct.setStock(20);
        testProduct.setDisponible(true);
        testProduct = productoStockRepository.save(testProduct);

        DetallePedido detalle = DetallePedido.builder()
                .productoId(testProduct.getId())
                .cantidad(2)
                .precioUnitario(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("20.00"))
                .build();

        pedidoPendiente = Pedido.builder()
                .clienteId(1L)
                .fechaPedido(Instant.now())
                .costoEnvio(new BigDecimal("20.00"))
                .montoTotal(new BigDecimal("40.00"))
                .estado(EstadoPedido.PENDIENTE)
                .build();

        pedidoPendiente.addDetalle(detalle);
        pedidoPendiente = pedidoRepository.save(pedidoPendiente);
    }

    // ─── CANCELACIÓN ────────────────────────────────────────────────────────────

    @Test
    void cancelarPedido_clientePropietario_shouldSucceed() throws Exception {
        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/cancelar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        // Stock restaurado
        ProductoStock stock = productoStockRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(22, stock.getStock());
    }

    @Test
    void cancelarPedido_clienteAjeno_shouldReturn403() throws Exception {
        // clienteId=99, no es el dueño del pedido (clienteId=1)
        String token = jwtService.generateToken(99L, "otro@test.com", "CLIENTE");

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/cancelar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelarPedido_adminAnyPedido_shouldSucceed() throws Exception {
        String token = jwtService.generateToken(999L, "admin@test.com", "ADMIN");

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/cancelar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    void cancelarPedido_noEnPendiente_shouldReturn400() throws Exception {
        pedidoPendiente.setEstado(EstadoPedido.EN_PREPARACION);
        pedidoRepository.save(pedidoPendiente);

        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/cancelar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ─── CAMBIO DE ESTADO ────────────────────────────────────────────────────────

    @Test
    void cambiarEstado_adminPENDIENTE_to_EN_PREPARACION_shouldSucceed() throws Exception {
        String token = jwtService.generateToken(999L, "admin@test.com", "ADMIN");
        CambioEstadoRequest request = new CambioEstadoRequest(EstadoPedido.EN_PREPARACION);

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/estado")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));
    }

    @Test
    void cambiarEstado_transicionInvalida_shouldReturn400() throws Exception {
        String token = jwtService.generateToken(999L, "admin@test.com", "ADMIN");
        // PENDIENTE → EN_CAMINO es inválido (debe pasar por EN_PREPARACION)
        CambioEstadoRequest request = new CambioEstadoRequest(EstadoPedido.EN_CAMINO);

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/estado")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarEstado_repartidorPedidoAjeno_shouldReturn403() throws Exception {
        // Asignar a repartidor 10
        pedidoPendiente.setEstado(EstadoPedido.EN_PREPARACION);
        pedidoPendiente.setRepartidorId(10L);
        pedidoRepository.save(pedidoPendiente);

        // Repartidor 20 intenta modificar
        String token = jwtService.generateToken(20L, "repartidor2@test.com", "REPARTIDOR");
        CambioEstadoRequest request = new CambioEstadoRequest(EstadoPedido.EN_CAMINO);

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/estado")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cambiarEstado_repartidorPedidoPropio_shouldSucceed() throws Exception {
        pedidoPendiente.setEstado(EstadoPedido.EN_PREPARACION);
        pedidoPendiente.setRepartidorId(10L);
        pedidoRepository.save(pedidoPendiente);

        String token = jwtService.generateToken(10L, "repartidor@test.com", "REPARTIDOR");
        CambioEstadoRequest request = new CambioEstadoRequest(EstadoPedido.EN_CAMINO);

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/estado")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CAMINO"));
    }

    @Test
    void cambiarEstado_clienteRole_shouldReturn403() throws Exception {
        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");
        CambioEstadoRequest request = new CambioEstadoRequest(EstadoPedido.EN_PREPARACION);

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/estado")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
