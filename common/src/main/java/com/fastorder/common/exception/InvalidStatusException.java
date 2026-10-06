package com.fastorder.common.exception;

/** Transición o estado de pedido inválido (HTTP 409). */
public class InvalidStatusException extends RuntimeException {
    public InvalidStatusException(String message) {
        super(message);
    }
}
