package com.botica.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "aviso_mantenimiento")
@Data
@NoArgsConstructor
public class AvisoMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String mensaje;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private Boolean activo = false;
}