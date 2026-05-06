package com.botica.repository;

import com.botica.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContaining(String nombre);

    // Productos con stock menor al mínimo
    List<Producto> findByStockLessThan(Integer stock);

    // Productos que vencen antes de una fecha
    @Query("SELECT p FROM Producto p WHERE " +
            "p.fechaVencimiento IS NOT NULL AND " +
            "p.fechaVencimiento <= :fecha")
    List<Producto> findProductosPorVencer(
            @Param("fecha") LocalDate fecha);

    // Productos ya vencidos
    @Query("SELECT p FROM Producto p WHERE " +
            "p.fechaVencimiento IS NOT NULL AND " +
            "p.fechaVencimiento < :hoy")
    List<Producto> findProductosVencidos(
            @Param("hoy") LocalDate hoy);
}