package com.fastorder.common.exception;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

/** Formato único de error REST para todos los microservicios. */
@Getter
@Builder
public class ErrorResponse {
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
}
