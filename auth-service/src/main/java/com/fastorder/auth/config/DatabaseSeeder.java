package com.fastorder.auth.config;

import com.fastorder.auth.domain.entity.Usuario;
import com.fastorder.auth.repository.UserRepository;
import com.fastorder.common.domain.Rol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String passwordHash = passwordEncoder.encode("admin123");
            
            if (userRepository.count() == 0) {
                log.info("Base de datos de usuarios vacía. Poblando usuarios iniciales...");

                userRepository.save(Usuario.builder()
                        .nombre("Admin Local")
                        .direccion("Central")
                        .telefono("00000000")
                        .email("admin@fastorder.com")
                        .password(passwordHash)
                        .rol(Rol.ADMIN)
                        .build());

                userRepository.save(Usuario.builder()
                        .nombre("Repartidor Juan")
                        .direccion("Zona 1")
                        .telefono("11111111")
                        .email("juan@fastorder.com")
                        .password(passwordHash)
                        .rol(Rol.REPARTIDOR)
                        .build());

                userRepository.save(Usuario.builder()
                        .nombre("Cliente Maria")
                        .direccion("Zona 2")
                        .telefono("22222222")
                        .email("maria@fastorder.com")
                        .password(passwordHash)
                        .rol(Rol.CLIENTE)
                        .build());

                log.info("Usuarios iniciales creados exitosamente (password: admin123).");
            } else {
                log.info("Actualizando contraseñas de usuarios existentes a 'admin123' por seguridad...");
                for (Usuario u : userRepository.findAll()) {
                    u.setPassword(passwordHash);
                    userRepository.save(u);
                }
            }
        };
    }
}
