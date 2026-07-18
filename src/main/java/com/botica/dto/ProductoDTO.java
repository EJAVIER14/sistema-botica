package com.botica.dto;

import java.time.LocalDate;

public record ProductoDTO(
        String nombre,
        String descripcion,
        Integer stock,
        LocalDate fechaVencimiento,
        String categoria,
        String lote,
        Double costo,
        Double margenGanancia,
        Integer stockMinimo
) {}