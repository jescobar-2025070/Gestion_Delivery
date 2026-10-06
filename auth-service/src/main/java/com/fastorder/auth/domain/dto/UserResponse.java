package com.fastorder.auth.domain.dto;

import com.fastorder.common.domain.Rol;

public record UserResponse(
        Long id,
        String nombre,
        String email,
        Rol rol
) {
}
