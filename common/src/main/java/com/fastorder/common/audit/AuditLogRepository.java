package com.fastorder.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntidad(String entidad, Pageable pageable);
    Page<AuditLog> findByActorId(Long actorId, Pageable pageable);
}
