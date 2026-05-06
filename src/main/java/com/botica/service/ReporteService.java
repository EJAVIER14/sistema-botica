package com.botica.service;

import com.botica.model.Venta;
import com.botica.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReporteService {

    @Autowired
    private VentaRepository ventaRepository;

    // Ventas entre dos fechas
    public List<Venta> ventasEntreFechas(
            LocalDate inicio, LocalDate fin) {

        LocalDateTime desde = inicio
                .atStartOfDay();
        LocalDateTime hasta = fin
                .atTime(LocalTime.MAX);

        return ventaRepository
                .findByFechaBetween(desde, hasta);
    }

    // Total ingresos entre dos fechas
    public Double totalIngresos(
            LocalDate inicio, LocalDate fin) {

        LocalDateTime desde = inicio
                .atStartOfDay();
        LocalDateTime hasta = fin
                .atTime(LocalTime.MAX);

        Double total = ventaRepository
                .totalVentasEntreFechas(desde, hasta);
        return total != null ? total : 0.0;
    }

    // Cantidad de ventas entre dos fechas
    public Long cantidadVentas(
            LocalDate inicio, LocalDate fin) {

        LocalDateTime desde = inicio
                .atStartOfDay();
        LocalDateTime hasta = fin
                .atTime(LocalTime.MAX);

        return ventaRepository
                .contarVentasEntreFechas(desde, hasta);
    }

    // Productos más vendidos
    public List<Object[]> productosMasVendidos() {
        return ventaRepository.productosMasVendidos();
    }

    // Resumen del día de hoy
    public Double totalHoy() {
        LocalDate hoy = LocalDate.now();
        return totalIngresos(hoy, hoy);
    }

    public Long ventasHoy() {
        LocalDate hoy = LocalDate.now();
        return cantidadVentas(hoy, hoy);
    }
}