package com.botica.repository;

import com.botica.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository
        extends JpaRepository<Venta, Long> {

    // Ventas entre dos fechas
    List<Venta> findByFechaBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // Total de ventas del día
    @Query("SELECT COALESCE(SUM(v.total), 0) " +
            "FROM Venta v WHERE v.fecha >= :inicio " +
            "AND v.fecha <= :fin")
    Double totalVentasEntreFechas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Productos más vendidos
    @Query("SELECT d.producto.nombre, " +
            "SUM(d.cantidad) as total " +
            "FROM DetalleVenta d " +
            "GROUP BY d.producto.nombre " +
            "ORDER BY total DESC")
    List<Object[]> productosMasVendidos();

    // Contar ventas del día
    @Query("SELECT COUNT(v) FROM Venta v " +
            "WHERE v.fecha >= :inicio " +
            "AND v.fecha <= :fin")
    Long contarVentasEntreFechas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}