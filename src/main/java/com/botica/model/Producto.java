package com.botica.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ═══ NUEVO: código único de producto (ej: BOT-0001) ═══
    @Column(unique = true, length = 20)
    private String codigo;

    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private LocalDate fechaVencimiento;
    private String categoria;

    // ═══ presentaciones de venta ═══
    private Integer unidadesPorBlister;
    private Integer unidadesPorCaja;
}