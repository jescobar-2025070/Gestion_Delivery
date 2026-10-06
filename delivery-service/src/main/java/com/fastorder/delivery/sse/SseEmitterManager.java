package com.fastorder.delivery.sse;

import com.fastorder.delivery.domain.dto.SseEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Gestor central de conexiones SSE.
 * Mantiene un mapa pedidoId → conjunto de emisores activos.
 * Thread-safe y libre de fugas de memoria: limpia automáticamente al cerrar/timeout.
 */
@Component
public class SseEmitterManager {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterManager.class);

    /** Timeout de 5 minutos para cada emisor. */
    private static final long SSE_TIMEOUT_MS = 5L * 60 * 1000;

    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long pedidoId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitters.computeIfAbsent(pedidoId, k -> new CopyOnWriteArraySet<>()).add(emitter);

        Runnable cleanup = () -> removeEmitter(pedidoId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ex -> {
            log.debug("Error en SSE emitter para pedido {}: {}", pedidoId, ex.getMessage());
            cleanup.run();
        });

        return emitter;
    }

    public void emit(Long pedidoId, SseEventDto event) {
        Set<SseEmitter> pedidoEmitters = emitters.getOrDefault(pedidoId, Set.of());
        if (pedidoEmitters.isEmpty()) return;

        Set<SseEmitter> dead = ConcurrentHashMap.newKeySet();
        for (SseEmitter emitter : pedidoEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name("estado-pedido")
                        .data(event, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                log.debug("Emitter muerto para pedido {}, eliminando.", pedidoId);
                dead.add(emitter);
            }
        }
        pedidoEmitters.removeAll(dead);
        if (pedidoEmitters.isEmpty()) {
            emitters.remove(pedidoId);
        }
    }

    private void removeEmitter(Long pedidoId, SseEmitter emitter) {
        Set<SseEmitter> set = emitters.get(pedidoId);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) {
                emitters.remove(pedidoId);
            }
        }
    }
}
