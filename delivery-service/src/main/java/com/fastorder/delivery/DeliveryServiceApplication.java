package com.fastorder.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.fastorder.common", "com.fastorder.delivery"})
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {"com.fastorder.common.audit", "com.fastorder.delivery.domain.entity"})
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = {"com.fastorder.common.audit", "com.fastorder.delivery.repository"})
public class DeliveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryServiceApplication.class, args);
    }
}
