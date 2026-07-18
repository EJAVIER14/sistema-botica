package com.botica.dto;

import java.time.LocalDate;

public record ProductoDTO(
        String nombre,
        String descripcion,
        Double precio,
        Integer stock,
        LocalDate fechaVencimiento,
        String categoria,
        String lote,
        Double costo
) {}