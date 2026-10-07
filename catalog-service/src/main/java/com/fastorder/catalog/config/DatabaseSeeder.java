package com.fastorder.catalog.config;

import com.fastorder.catalog.domain.entity.Comercio;
import com.fastorder.catalog.domain.entity.Producto;
import com.fastorder.catalog.domain.enums.Categoria;
import com.fastorder.catalog.repository.ComercioRepository;
import com.fastorder.catalog.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DatabaseSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Bean
    public CommandLineRunner initDatabase(ComercioRepository comercioRepository, ProductoRepository productoRepository) {
        return args -> {
            if (comercioRepository.count() == 0) {
                log.info("Base de datos de comercios vacía. Poblando datos iniciales...");

                Comercio comercio = Comercio.builder()
                        .nombre("Super Burger")
                        .categoria(Categoria.RESTAURANTE)
                        .direccion("Calle 10, Local A")
                        .abierto(true)
                        .build();

                comercio = comercioRepository.save(comercio);

                productoRepository.save(Producto.builder()
                        .comercio(comercio)
                        .nombre("Hamburguesa Doble")
                        .precio(new BigDecimal("45.00"))
                        .stock(100)
                        .disponible(true)
                        .build());

                productoRepository.save(Producto.builder()
                        .comercio(comercio)
                        .nombre("Papas Fritas")
                        .precio(new BigDecimal("15.00"))
                        .stock(200)
                        .disponible(true)
                        .build());

                productoRepository.save(Producto.builder()
                        .comercio(comercio)
                        .nombre("Gaseosa")
                        .precio(new BigDecimal("10.00"))
                        .stock(150)
                        .disponible(true)
                        .build());

                log.info("Comercio y productos iniciales creados exitosamente.");
            }
        };
    }
}
