package com.fastorder.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Registro de auditoría de operaciones administrativas.
 * No almacena passwords, tokens ni información sensible.
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_actor_id", columnList = "actorId"),
        @Index(name = "idx_audit_entidad", columnList = "entidad"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del usuario que realizó la operación. */
    @Column(nullable = false)
    private Long actorId;

    /** Email del actor (sin contraseña ni token). */
    @Column(nullable = false)
    private String actorEmail;

    /** Tipo de entidad afectada: COMERCIO, PRODUCTO, PEDIDO, etc. */
    @Column(nullable = false)
    private String entidad;

    /** ID de la entidad afectada. */
    private Long entidadId;

    /** Descripción de la operación: CREAR_COMERCIO, CANCELAR_PEDIDO, etc. */
    @Column(nullable = false)
    private String operacion;

    /** Detalle adicional sin datos sensibles. */
    @Column(length = 512)
    private String detalle;

    @Column(nullable = false)
    private Instant timestamp;
}
