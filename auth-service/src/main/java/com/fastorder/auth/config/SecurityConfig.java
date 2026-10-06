package com.fastorder.auth.config;

import com.fastorder.common.domain.AppConstants;
import com.fastorder.common.security.StatelessSecuritySupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        StatelessSecuritySupport.apply(http)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,
                                AppConstants.API_PREFIX + "/auth/register",
                                AppConstants.API_PREFIX + "/auth/login").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
