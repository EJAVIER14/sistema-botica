package com.botica.service;

import com.botica.model.Venta;
import com.botica.repository.ProductoRepository;
import com.botica.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Venta> ventasEntreFechas(LocalDate inicio, LocalDate fin) {
        return ventaRepository.findByFechaBetween(
                inicio.atStartOfDay(),
                fin.atTime(LocalTime.MAX));
    }

    public Double totalIngresos(LocalDate inicio, LocalDate fin) {
        Double total = ventaRepository.totalVentasEntreFechas(
                inicio.atStartOfDay(),
                fin.atTime(LocalTime.MAX));
        return total != null ? total : 0.0;
    }

    public Long cantidadVentas(LocalDate inicio, LocalDate fin) {
        return ventaRepository.contarVentasEntreFechas(
                inicio.atStartOfDay(),
                fin.atTime(LocalTime.MAX));
    }

    public List<Object[]> productosMasVendidos() {
        return ventaRepository.productosMasVendidos();
    }

    public Double totalHoy() {
        LocalDate hoy = LocalDate.now();
        return totalIngresos(hoy, hoy);
    }

    public Long ventasHoy() {
        LocalDate hoy = LocalDate.now();
        return cantidadVentas(hoy, hoy);
    }

    public List<Venta> ultimasVentas() {
        List<Venta> todas = ventaRepository.findAll();
        int size = todas.size();
        return todas.subList(Math.max(0, size - 5), size);
    }

    public Long stockCritico() {
        return (long) productoRepository.findByStockLessThan(10).size();
    }

    public Long productosPorVencer() {
        return (long) productoRepository
                .findProductosPorVencer(LocalDate.now().plusDays(30)).size();
    }

    // Datos agrupados por día para el gráfico de ventas
    public Map<String, Double> ventasPorDia(LocalDate inicio, LocalDate fin) {
        List<Venta> ventas = ventasEntreFechas(inicio, fin);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        // Inicializar todos los días del período en 0
        Map<String, Double> resultado = new LinkedHashMap<>();
        LocalDate dia = inicio;
        while (!dia.isAfter(fin)) {
            resultado.put(dia.format(fmt), 0.0);
            dia = dia.plusDays(1);
        }

        // Sumar ventas por día
        for (Venta v : ventas) {
            String key = v.getFecha().toLocalDate().format(fmt);
            resultado.merge(key, v.getTotal(), Double::sum);
        }

        return resultado;
    }
}