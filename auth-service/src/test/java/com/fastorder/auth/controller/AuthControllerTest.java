package com.fastorder.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fastorder.auth.domain.dto.LoginRequest;
import com.fastorder.auth.domain.dto.RegisterRequest;
import com.fastorder.auth.domain.entity.Usuario;
import com.fastorder.auth.repository.UserRepository;
import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.domain.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest("Juan", "Direccion", "12345678", "juan@test.com", "password123");

        mockMvc.perform(post(AppConstants.API_PREFIX + "/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.email").value("juan@test.com"))
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"));
    }

    @Test
    void login_success() throws Exception {
        Usuario usuario = Usuario.builder()
                .nombre("Admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.ADMIN)
                .build();
        userRepository.save(usuario);

        LoginRequest request = new LoginRequest("admin@test.com", "admin123");

        mockMvc.perform(post(AppConstants.API_PREFIX + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.email").value("admin@test.com"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"));
    }

    @Test
    void login_invalidCredentials() throws Exception {
        Usuario usuario = Usuario.builder()
                .nombre("Admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.ADMIN)
                .build();
        userRepository.save(usuario);

        LoginRequest request = new LoginRequest("admin@test.com", "wrongpassword");

        mockMvc.perform(post(AppConstants.API_PREFIX + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
