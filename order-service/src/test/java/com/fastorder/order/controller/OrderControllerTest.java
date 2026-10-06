package com.fastorder.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.security.JwtService;
import com.fastorder.order.domain.dto.PedidoRequest;
import com.fastorder.order.domain.entity.ProductoStock;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductoStockRepository productoStockRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testProductId;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        productoStockRepository.deleteAll();

        ProductoStock p = new ProductoStock();
        p.setId(10L);
        p.setNombre("Burger");
        p.setPrecio(new BigDecimal("50.00"));
        p.setStock(5);
        p.setDisponible(true);

        p = productoStockRepository.save(p);
        testProductId = p.getId();
    }

    @Test
    void createPedido_success() throws Exception {
        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");

        PedidoRequest request = new PedidoRequest(List.of(
                new PedidoRequest.ItemRequest(testProductId, 2)
        ));

        mockMvc.perform(post(AppConstants.API_PREFIX + "/pedidos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costoEnvio").value("20.0"))
                .andExpect(jsonPath("$.montoTotal").value("120.0")); // (50 * 2) + 20

        ProductoStock stock = productoStockRepository.findById(testProductId).orElseThrow();
        assert stock.getStock() == 3;
    }

    @Test
    void createPedido_insufficientStock() throws Exception {
        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");

        PedidoRequest request = new PedidoRequest(List.of(
                new PedidoRequest.ItemRequest(testProductId, 10)
        ));

        mockMvc.perform(post(AppConstants.API_PREFIX + "/pedidos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        ProductoStock stock = productoStockRepository.findById(testProductId).orElseThrow();
        assert stock.getStock() == 5; // Rollback
    }

    @Test
    void getMisPedidos_success() throws Exception {
        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/pedidos/mis-pedidos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
