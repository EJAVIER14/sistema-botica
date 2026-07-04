package com.botica.service;

import com.botica.dto.ProductoDTO;
import com.botica.exception.FechaVencimientoInvalidaException;
import com.botica.exception.NombreInvalidoException;
import com.botica.exception.PrecioInvalidoException;
import com.botica.exception.ProductoDuplicadoException;
import com.botica.exception.StockInsuficienteException;
import com.botica.exception.StockInvalidoException;
import com.botica.model.Presentacion;
import com.botica.model.Producto;
import com.botica.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repo;

    @InjectMocks
    private ProductoService productoService;

    @Test
    @DisplayName("Debe lanzar excepción al crear un producto con nombre ya existente")
    void deberiaLanzarExcepcionSiElNombreYaExiste() {
        ProductoDTO dto = new ProductoDTO(
                "Paracetamol 500mg",
                "Analgésico y antipirético",
                5.50,
                100,
                LocalDate.now().plusYears(1),
                "Analgésicos"
        );

        when(repo.existsByNombre("Paracetamol 500mg")).thenReturn(true);

        assertThrows(ProductoDuplicadoException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe guardar el producto correctamente si el nombre no existe")
    void deberiaGuardarProductoSiNoExisteElNombre() {
        ProductoDTO dto = new ProductoDTO(
                "Ibuprofeno 400mg",
                "Antiinflamatorio",
                3.20,
                50,
                LocalDate.now().plusMonths(8),
                "Antiinflamatorios"
        );

        when(repo.existsByNombre("Ibuprofeno 400mg")).thenReturn(false);
        when(repo.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.crear(dto);

        assertEquals("Ibuprofeno 400mg", resultado.getNombre());
        assertEquals(3.20, resultado.getPrecio());
        verify(repo, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear un producto con precio negativo")
    void deberiaLanzarExcepcionSiElPrecioEsNegativo() {
        ProductoDTO dto = new ProductoDTO(
                "Amoxicilina 500mg",
                "Antibiótico",
                -10.0,
                30,
                LocalDate.now().plusMonths(6),
                "Antibióticos"
        );

        assertThrows(PrecioInvalidoException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear un producto con precio igual a cero")
    void deberiaLanzarExcepcionSiElPrecioEsCero() {
        ProductoDTO dto = new ProductoDTO(
                "Vitamina C 1g",
                "Suplemento",
                0.0,
                30,
                LocalDate.now().plusMonths(6),
                "Suplementos"
        );

        assertThrows(PrecioInvalidoException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear un producto con stock negativo")
    void deberiaLanzarExcepcionSiElStockEsNegativo() {
        ProductoDTO dto = new ProductoDTO(
                "Omeprazol 20mg",
                "Protector gástrico",
                4.00,
                -5,
                LocalDate.now().plusMonths(10),
                "Gastroprotectores"
        );

        assertThrows(StockInvalidoException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear un producto con nombre vacío")
    void deberiaLanzarExcepcionSiElNombreEstaVacio() {
        ProductoDTO dto = new ProductoDTO(
                "   ",
                "Descripcion cualquiera",
                5.0,
                10,
                LocalDate.now().plusMonths(6),
                "Otro"
        );

        assertThrows(NombreInvalidoException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear un producto con fecha de vencimiento ya pasada")
    void deberiaLanzarExcepcionSiLaFechaDeVencimientoYaPaso() {
        ProductoDTO dto = new ProductoDTO(
                "Producto Vencido",
                "Descripcion cualquiera",
                5.0,
                10,
                LocalDate.now().minusDays(1),
                "Otro"
        );

        when(repo.existsByNombre("Producto Vencido")).thenReturn(false);

        assertThrows(FechaVencimientoInvalidaException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    // ═══════════ NUEVOS: Presentaciones de venta ═══════════

    @Test
    @DisplayName("Debe calcular correctamente las unidades a descontar al vender por BLISTER")
    void deberiaCalcularUnidadesAlVenderPorBlister() {
        Producto producto = new Producto();
        producto.setStock(100);
        producto.setUnidadesPorBlister(10);
        producto.setUnidadesPorCaja(100);

        int unidades = productoService.calcularUnidades(producto, Presentacion.BLISTER, 2);

        assertEquals(20, unidades); // 2 blisters x 10 unidades c/u
    }

    @Test
    @DisplayName("Debe calcular correctamente las unidades a descontar al vender por CAJA")
    void deberiaCalcularUnidadesAlVenderPorCaja() {
        Producto producto = new Producto();
        producto.setStock(500);
        producto.setUnidadesPorBlister(10);
        producto.setUnidadesPorCaja(100);

        int unidades = productoService.calcularUnidades(producto, Presentacion.CAJA, 1);

        assertEquals(100, unidades); // 1 caja = 100 unidades
    }

    @Test
    @DisplayName("Debe calcular correctamente las unidades al vender por UNIDAD suelta")
    void deberiaCalcularUnidadesAlVenderPorUnidad() {
        Producto producto = new Producto();
        producto.setStock(50);

        int unidades = productoService.calcularUnidades(producto, Presentacion.UNIDAD, 5);

        assertEquals(5, unidades); // 5 pastillas sueltas
    }

    @Test
    @DisplayName("Debe calcular el precio total correctamente segun la presentacion")
    void deberiaCalcularPrecioTotalSegunPresentacion() {
        Producto producto = new Producto();
        producto.setPrecio(0.50); // precio por unidad
        producto.setUnidadesPorBlister(10);
        producto.setUnidadesPorCaja(100);

        double precioBlister = productoService.calcularPrecioTotal(producto, Presentacion.BLISTER, 1);
        double precioCaja = productoService.calcularPrecioTotal(producto, Presentacion.CAJA, 1);
        double precioUnidad = productoService.calcularPrecioTotal(producto, Presentacion.UNIDAD, 3);

        assertEquals(5.0, precioBlister);   // 10 unidades x 0.50
        assertEquals(50.0, precioCaja);     // 100 unidades x 0.50
        assertEquals(1.5, precioUnidad);    // 3 unidades x 0.50
    }

    @Test
    @DisplayName("Debe lanzar excepcion si no hay stock suficiente al vender")
    void deberiaLanzarExcepcionSiNoHayStockSuficiente() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Ibuprofeno 400mg");
        producto.setStock(5);
        producto.setUnidadesPorBlister(10);

        when(repo.findById(1L)).thenReturn(Optional.of(producto));

        // Intenta vender 1 blister (10 unidades) pero solo hay 5 en stock
        assertThrows(StockInsuficienteException.class,
                () -> productoService.venderPorPresentacion(1L, Presentacion.BLISTER, 1));

        verify(repo, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe descontar el stock correctamente al vender por presentacion")
    void deberiaDescontarStockAlVenderPorPresentacion() {
        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Amoxicilina 500mg");
        producto.setStock(100);
        producto.setUnidadesPorBlister(10);

        when(repo.findById(2L)).thenReturn(Optional.of(producto));
        when(repo.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.venderPorPresentacion(2L, Presentacion.BLISTER, 3);

        assertEquals(70, resultado.getStock()); // 100 - (3 blisters x 10) = 70
        verify(repo, times(1)).save(any(Producto.class));
    }
}