package com.fastorder.catalog.repository;

import com.fastorder.catalog.domain.entity.Comercio;
import com.fastorder.catalog.domain.enums.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComercioRepository extends JpaRepository<Comercio, Long> {
    Page<Comercio> findByAbiertoTrue(Pageable pageable);
    Page<Comercio> findByAbiertoTrueAndCategoria(Categoria categoria, Pageable pageable);
}
