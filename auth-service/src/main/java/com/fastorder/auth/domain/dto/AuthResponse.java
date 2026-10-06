package com.fastorder.auth.domain.dto;

public record AuthResponse(
        String token,
        UserResponse usuario
) {
}
