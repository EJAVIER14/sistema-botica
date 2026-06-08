package com.botica.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Data
@NoArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private String tipo; // ENTRADA, SALIDA
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String motivo; // VENTA, REABASTECIMIENTO, AJUSTE
    private String usuario;
    private LocalDateTime fecha;
}