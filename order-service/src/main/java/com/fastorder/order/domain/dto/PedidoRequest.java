package com.fastorder.order.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PedidoRequest(
        @NotEmpty
        @Valid
        @JsonAlias("items")
        List<ItemRequest> productos
) {
    public record ItemRequest(
            @NotNull Long productoId,
            @NotNull @Positive Integer cantidad
    ) {}
}