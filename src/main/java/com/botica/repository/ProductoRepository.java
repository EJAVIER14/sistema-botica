package com.botica.repository;

import com.botica.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Búsqueda paginada por nombre (ignora mayúsculas/minúsculas)
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // Búsqueda paginada por nombre O código
    Page<Producto> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(
            String nombre, String codigo, Pageable pageable);

    List<Producto> findByStockLessThan(Integer stock);

    // Nuevo: para validar duplicados antes de crear un producto
    boolean existsByNombre(String nombre);

    @Query("SELECT p FROM Producto p WHERE " +
            "p.fechaVencimiento IS NOT NULL AND " +
            "p.fechaVencimiento <= :fecha")
    List<Producto> findProductosPorVencer(@Param("fecha") LocalDate fecha);

    @Query("SELECT p FROM Producto p WHERE " +
            "p.fechaVencimiento IS NOT NULL AND " +
            "p.fechaVencimiento < :hoy")
    List<Producto> findProductosVencidos(@Param("hoy") LocalDate hoy);

    // Para la migración de códigos de productos existentes
    List<Producto> findByCodigoIsNull();

    // Para buscar un producto directamente por su código (útil en el POS)
    Producto findByCodigo(String codigo);

    // ═══ NUEVO: búsqueda combinada con filtro opcional de texto y categoría ═══
    @Query("SELECT p FROM Producto p WHERE " +
            "(:buscar IS NULL OR :buscar = '' OR " +
            " LOWER(p.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
            " LOWER(p.codigo) LIKE LOWER(CONCAT('%', :buscar, '%'))) AND " +
            "(:categoria IS NULL OR :categoria = '' OR p.categoria = :categoria)")
    Page<Producto> buscarConFiltros(
            @Param("buscar") String buscar,
            @Param("categoria") String categoria,
            Pageable pageable);

    // ═══ NUEVO: lista de categorías distintas existentes, para el dropdown ═══
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.categoria IS NOT NULL ORDER BY p.categoria")
    List<String> listarCategoriasDistintas();
}