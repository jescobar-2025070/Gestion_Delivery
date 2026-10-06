package com.fastorder.common.security;

public record AuthenticatedUser(Long id, String email, String role) {
}
