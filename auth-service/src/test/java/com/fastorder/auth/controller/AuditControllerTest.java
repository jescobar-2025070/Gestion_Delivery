package com.fastorder.auth.controller;

import com.fastorder.common.audit.AuditLog;
import com.fastorder.common.audit.AuditLogRepository;
import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();

        AuditLog log1 = AuditLog.builder()
                .actorId(1L)
                .actorEmail("admin@test.com")
                .entidad("COMERCIO")
                .entidadId(10L)
                .operacion("CREAR_COMERCIO")
                .detalle("test 1")
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(log1);
    }

    @Test
    void getAuditorias_admin_shouldSucceed() throws Exception {
        String token = jwtService.generateToken(1L, "admin@test.com", "ADMIN");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/auditoria")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].operacion").value("CREAR_COMERCIO"));
    }

    @Test
    void getAuditorias_repartidor_shouldReturn403() throws Exception {
        String token = jwtService.generateToken(2L, "rep@test.com", "REPARTIDOR");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/auditoria")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
