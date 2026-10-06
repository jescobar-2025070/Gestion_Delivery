package com.fastorder.delivery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.security.JwtService;
import com.fastorder.delivery.domain.dto.ActualizarEstadoRequest;
import com.fastorder.delivery.domain.entity.PedidoDelivery;
import com.fastorder.delivery.domain.enums.EstadoPedido;
import com.fastorder.delivery.repository.PedidoDeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PedidoDeliveryRepository pedidoDeliveryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PedidoDelivery pedidoPendiente;
    private PedidoDelivery pedidoAsignado;

    @BeforeEach
    void setUp() {
        pedidoDeliveryRepository.deleteAll();

        pedidoPendiente = new PedidoDelivery();
        pedidoPendiente.setId(1L);
        pedidoPendiente.setClienteId(1L);
        pedidoPendiente.setRepartidorId(null);
        pedidoPendiente.setFechaPedido(Instant.now());
        pedidoPendiente.setMontoTotal(new BigDecimal("120.00"));
        pedidoPendiente.setEstado(EstadoPedido.PENDIENTE);
        pedidoPendiente = pedidoDeliveryRepository.save(pedidoPendiente);

        pedidoAsignado = new PedidoDelivery();
        pedidoAsignado.setId(2L);
        pedidoAsignado.setClienteId(2L);
        pedidoAsignado.setRepartidorId(10L); // asignado al repartidor 10
        pedidoAsignado.setFechaPedido(Instant.now());
        pedidoAsignado.setMontoTotal(new BigDecimal("80.00"));
        pedidoAsignado.setEstado(EstadoPedido.EN_PREPARACION);
        pedidoAsignado = pedidoDeliveryRepository.save(pedidoAsignado);
    }

    @Test
    void getPedidosDisponibles_repartidor_shouldSeeUnassignedAndOwn() throws Exception {
        // Repartidor 10 debe ver el pendiente (sin asignar) y el suyo (asignado a 10)
        String token = jwtService.generateToken(10L, "repartidor@test.com", "REPARTIDOR");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/pedidos/disponibles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getPedidosDisponibles_admin_shouldSeeAll() throws Exception {
        String token = jwtService.generateToken(99L, "admin@test.com", "ADMIN");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/pedidos/disponibles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getPedidosDisponibles_cliente_shouldReturn403() throws Exception {
        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/pedidos/disponibles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarEstado_repartidorPedidoAsignado_shouldSucceed() throws Exception {
        String token = jwtService.generateToken(10L, "repartidor@test.com", "REPARTIDOR");
        ActualizarEstadoRequest request = new ActualizarEstadoRequest(EstadoPedido.EN_CAMINO);

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoAsignado.getId() + "/estado")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CAMINO"));
    }

    @Test
    void actualizarEstado_repartidorPedidoAjeno_shouldReturn403() throws Exception {
        // Repartidor 20 intenta modificar pedido asignado al repartidor 10
        String token = jwtService.generateToken(20L, "otro@test.com", "REPARTIDOR");
        ActualizarEstadoRequest request = new ActualizarEstadoRequest(EstadoPedido.EN_CAMINO);

        mockMvc.perform(patch(AppConstants.API_PREFIX + "/pedidos/" + pedidoAsignado.getId() + "/estado")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void streamEstadoPedido_clientePropietario_shouldReturn200() throws Exception {
        // Cliente 1 es propietario del pedidoPendiente
        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/stream")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    void streamEstadoPedido_clienteAjeno_shouldReturn403() throws Exception {
        // Cliente 99 no es propietario del pedidoPendiente (cliente 1)
        String token = jwtService.generateToken(99L, "ajeno@test.com", "CLIENTE");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/pedidos/" + pedidoPendiente.getId() + "/stream")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isForbidden());
    }
}
