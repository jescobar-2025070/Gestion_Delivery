package com.fastorder.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.fastorder.common", "com.fastorder.order"})
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {"com.fastorder.common.audit", "com.fastorder.order.domain.entity"})
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = {"com.fastorder.common.audit", "com.fastorder.order.repository"})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
