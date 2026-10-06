package com.fastorder.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.fastorder.common", "com.fastorder.auth"})
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {"com.fastorder.common.audit", "com.fastorder.auth.domain.entity"})
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = {"com.fastorder.common.audit", "com.fastorder.auth.repository"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
