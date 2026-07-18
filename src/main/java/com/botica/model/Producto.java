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

    // Código único de producto (ej: BOT-0001)
    @Column(unique = true, length = 20)
    private String codigo;

    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private LocalDate fechaVencimiento;
    private String categoria;

    // Trazabilidad del lote de fabricación
    private String lote;

    // Costo de compra (precio al que la farmacia adquirió el producto)
    private Double costo;

    // Porcentaje de margen aplicado sobre el costo para calcular el precio de venta
    private Double margenGanancia;

    // ═══ NUEVO: punto de reorden configurable por producto (antes era un 10 fijo para todos) ═══
    private Integer stockMinimo = 10;

    // Presentaciones de venta
    private Integer unidadesPorBlister;
    private Integer unidadesPorCaja;
}