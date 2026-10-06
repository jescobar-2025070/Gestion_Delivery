package com.fastorder.catalog.repository;

import com.fastorder.catalog.domain.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Page<Producto> findByComercioIdAndDisponibleTrue(Long comercioId, Pageable pageable);
}
