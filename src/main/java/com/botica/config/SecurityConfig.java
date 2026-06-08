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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Públicas
                        .requestMatchers("/login", "/css/**", "/setup").permitAll()

                        // Solo ADMIN
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/reportes/**").hasRole("ADMIN")
                        .requestMatchers("/proveedores/**").hasRole("ADMIN")
                        .requestMatchers("/productos/nuevo").hasRole("ADMIN")
                        .requestMatchers("/productos/editar/**").hasRole("ADMIN")
                        .requestMatchers("/productos/eliminar/**").hasRole("ADMIN")

                        // ADMIN y VENDEDOR
                        .requestMatchers("/productos/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers("/ventas/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers("/alertas/**").hasAnyRole("ADMIN", "VENDEDOR")

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