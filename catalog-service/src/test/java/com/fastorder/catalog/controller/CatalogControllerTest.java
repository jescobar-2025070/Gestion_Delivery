package com.fastorder.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fastorder.catalog.domain.dto.ComercioRequest;
import com.fastorder.catalog.domain.dto.ProductoRequest;
import com.fastorder.catalog.domain.entity.Comercio;
import com.fastorder.catalog.domain.enums.Categoria;
import com.fastorder.catalog.repository.ComercioRepository;
import com.fastorder.catalog.repository.ProductoRepository;
import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComercioRepository comercioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
        comercioRepository.deleteAll();
    }

    @Test
    void getComercios_withClientRole_shouldReturn200() throws Exception {
        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/comercios")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void createComercio_withAdminRole_shouldReturn201() throws Exception {
        String token = jwtService.generateToken(1L, "admin@test.com", "ADMIN");
        ComercioRequest request = new ComercioRequest("Super Market", Categoria.SUPERMERCADO, "123 St", true);

        mockMvc.perform(post(AppConstants.API_PREFIX + "/comercios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Super Market"));
    }

    @Test
    void createComercio_withClientRole_shouldReturn403() throws Exception {
        String token = jwtService.generateToken(1L, "cliente@test.com", "CLIENTE");
        ComercioRequest request = new ComercioRequest("Super Market", Categoria.SUPERMERCADO, "123 St", true);

        mockMvc.perform(post(AppConstants.API_PREFIX + "/comercios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProducto_withAdminRole_shouldReturn201() throws Exception {
        String token = jwtService.generateToken(1L, "admin@test.com", "ADMIN");
        
        Comercio comercio = comercioRepository.save(Comercio.builder()
                .nombre("Farmacia 24H")
                .categoria(Categoria.FARMACIA)
                .direccion("123 St")
                .abierto(true)
                .build());

        ProductoRequest request = new ProductoRequest("Aspirina", new BigDecimal("5.50"), 100, true);

        mockMvc.perform(post(AppConstants.API_PREFIX + "/comercios/" + comercio.getId() + "/productos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Aspirina"));
    }
}
