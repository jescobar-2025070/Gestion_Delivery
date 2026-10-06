package com.fastorder.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.fastorder.common", "com.fastorder.catalog"})
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {"com.fastorder.common.audit", "com.fastorder.catalog.domain.entity"})
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = {"com.fastorder.common.audit", "com.fastorder.catalog.repository"})
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
