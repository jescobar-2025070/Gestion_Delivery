package com.fastorder.common.exception;

/** Stock insuficiente para atender un pedido (HTTP 409). */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
