package com.fastorder.auth.security;

import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void accessProtected_withoutJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get(AppConstants.API_PREFIX + "/test/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtected_withValidJwt_shouldReturn200() throws Exception {
        String token = jwtService.generateToken(1L, "user@test.com", "CLIENTE");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/test/protected")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void accessAdmin_withClienteRole_shouldReturn403() throws Exception {
        String token = jwtService.generateToken(1L, "user@test.com", "CLIENTE");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/test/admin")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessAdmin_withAdminRole_shouldReturn200() throws Exception {
        String token = jwtService.generateToken(1L, "admin@test.com", "ADMIN");

        mockMvc.perform(get(AppConstants.API_PREFIX + "/test/admin")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
