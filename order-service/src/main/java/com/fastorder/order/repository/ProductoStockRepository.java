package com.fastorder.order.repository;

import com.fastorder.order.domain.entity.ProductoStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoStockRepository extends JpaRepository<ProductoStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductoStock p WHERE p.id = :id")
    Optional<ProductoStock> findByIdForUpdate(@Param("id") Long id);
}
