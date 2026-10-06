package com.fastorder.order.controller;

import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.security.AuthenticatedUser;
import com.fastorder.order.domain.dto.PedidoRequest;
import com.fastorder.order.domain.dto.PedidoResponse;
import com.fastorder.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse createPedido(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody PedidoRequest request) {
        return orderService.createPedido(user.id(), request);
    }

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('CLIENTE')")
    public Page<PedidoResponse> getMisPedidos(
            @AuthenticationPrincipal AuthenticatedUser user,
            Pageable pageable) {
        return orderService.getMisPedidos(user.id(), pageable);
    }
}
