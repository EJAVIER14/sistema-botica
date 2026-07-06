package com.botica.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    private boolean usado = false;

    public PasswordResetToken(String token, Long usuarioId, LocalDateTime fechaExpiracion) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = false;
    }

    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
}