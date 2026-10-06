package com.fastorder.auth.service;

import com.fastorder.auth.domain.dto.AuthResponse;
import com.fastorder.auth.domain.dto.LoginRequest;
import com.fastorder.auth.domain.dto.RegisterRequest;
import com.fastorder.auth.domain.dto.UserResponse;
import com.fastorder.auth.domain.entity.Usuario;
import com.fastorder.auth.repository.UserRepository;
import com.fastorder.common.domain.Rol;
import com.fastorder.common.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.CLIENTE) // Por defecto CLIENTE
                .build();

        usuario = userRepository.save(usuario);

        String token = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol().name());
        
        return new AuthResponse(token, new UserResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        ));
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invlidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invlidas");
        }

        String token = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol().name());
        
        return new AuthResponse(token, new UserResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        ));
    }
}
