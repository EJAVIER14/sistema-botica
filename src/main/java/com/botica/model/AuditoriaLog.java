package com.botica.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_logs")
@Data
@NoArgsConstructor
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    private String modulo;      // ej: "auth"
    private String accion;      // ej: "LOGIN_EXITOSO", "LOGIN_FALLIDO", "LOGOUT"

    @Enumerated(EnumType.STRING)
    private EstadoAuditoria estado;

    private String usuario;     // username involucrado

    @Column(length = 500)
    private String descripcion; // mensaje legible para mostrar en la tabla

    public AuditoriaLog(String modulo, String accion, EstadoAuditoria estado, String usuario, String descripcion) {
        this.fecha = LocalDateTime.now();
        this.modulo = modulo;
        this.accion = accion;
        this.estado = estado;
        this.usuario = usuario;
        this.descripcion = descripcion;
    }
}