package com.botica.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalle_orden_entrada")
@Data
@NoArgsConstructor
public class DetalleOrdenEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer cantidad;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "orden_id")
    private OrdenEntrada orden;
}