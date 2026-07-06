package com.botica.config;

import com.botica.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // Públicas
                        .requestMatchers("/login", "/css/**", "/setup", "/olvide-password", "/restablecer-password").permitAll()

                        // Solo ADMIN
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/reportes/**").hasRole("ADMIN")
                        .requestMatchers("/inventario/**").hasRole("ADMIN")

                        // ADMIN y ALMACENERO
                        .requestMatchers("/proveedores/**").hasAnyRole("ADMIN", "ALMACENERO")
                        .requestMatchers("/productos/nuevo").hasAnyRole("ADMIN", "ALMACENERO")
                        .requestMatchers("/productos/editar/**").hasAnyRole("ADMIN", "ALMACENERO")
                        .requestMatchers("/productos/eliminar/**").hasAnyRole("ADMIN", "ALMACENERO")

                        // ADMIN, CAJERO y ALMACENERO
                        .requestMatchers("/productos/**").hasAnyRole("ADMIN", "CAJERO", "ALMACENERO")
                        .requestMatchers("/alertas/**").hasAnyRole("ADMIN", "CAJERO", "ALMACENERO")

                        // ADMIN y CAJERO
                        .requestMatchers("/ventas/**").hasAnyRole("ADMIN", "CAJERO")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/acceso-denegado")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }
}