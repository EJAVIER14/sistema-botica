package com.botica.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;
    private String cliente;
    private String dni;
    private String ruc;
    private String formaPago;
    private Double subtotal;
    private Double igv;
    private Double descuento;
    private Double total;
    private Double montoRecibido;
    private Double vuelto;

    @OneToMany(mappedBy = "venta",
            cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles;
}