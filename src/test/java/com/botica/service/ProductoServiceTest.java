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
                100,
                LocalDate.now().plusYears(1),
                "Analgésicos",
                "LT-2026-001",
                3.50,
                40.0,
                10
        );

        when(repo.existsByNombre("Paracetamol 500mg")).thenReturn(true);

        assertThrows(ProductoDuplicadoException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe guardar el producto correctamente si el nombre no existe, calculando el precio segun el margen")
    void deberiaGuardarProductoSiNoExisteElNombre() {
        ProductoDTO dto = new ProductoDTO(
                "Ibuprofeno 400mg",
                "Antiinflamatorio",
                50,
                LocalDate.now().plusMonths(8),
                "Antiinflamatorios",
                "LT-2026-002",
                2.00,  // costo
                60.0,  // margen 60%
                15     // stock minimo
        );

        when(repo.existsByNombre("Ibuprofeno 400mg")).thenReturn(false);

        when(repo.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(15L);
            }
            return p;
        });

        Producto resultado = productoService.crear(dto);

        assertEquals("Ibuprofeno 400mg", resultado.getNombre());
        assertEquals(2.00, resultado.getCosto());
        assertEquals(60.0, resultado.getMargenGanancia());
        assertEquals(3.20, resultado.getPrecio()); // 2.00 x 1.60 = 3.20
        assertEquals(15, resultado.getStockMinimo());
        assertEquals("BOT-0015", resultado.getCodigo());

        verify(repo, times(2)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe usar el stock minimo por defecto (10) si no se especifica en el DTO")
    void deberiaUsarStockMinimoPorDefectoSiEsNulo() {
        ProductoDTO dto = new ProductoDTO(
                "Diclofenaco 50mg",
                "Antiinflamatorio",
                40,
                LocalDate.now().plusMonths(5),
                "Antiinflamatorios",
                "LT-2026-009",
                1.80,
                50.0,
                null // no se especifica stock minimo
        );

        when(repo.existsByNombre("Diclofenaco 50mg")).thenReturn(false);

        when(repo.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(20L);
            }
            return p;
        });

        Producto resultado = productoService.crear(dto);

        assertEquals(10, resultado.getStockMinimo()); // valor por defecto

        verify(repo, times(2)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear un producto con costo negativo")
    void deberiaLanzarExcepcionSiElCostoEsNegativo() {
        ProductoDTO dto = new ProductoDTO(
                "Amoxicilina 500mg",
                "Antibiótico",
                30,
                LocalDate.now().plusMonths(6),
                "Antibióticos",
                "LT-2026-003",
                -10.0,
                20.0,
                10
        );

        assertThrows(PrecioInvalidoException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear un producto con costo igual a cero")
    void deberiaLanzarExcepcionSiElCostoEsCero() {
        ProductoDTO dto = new ProductoDTO(
                "Vitamina C 1g",
                "Suplemento",
                30,
                LocalDate.now().plusMonths(6),
                "Suplementos",
                "LT-2026-004",
                0.0,
                20.0,
                10
        );

        assertThrows(PrecioInvalidoException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear un producto con margen de ganancia negativo")
    void deberiaLanzarExcepcionSiElMargenEsNegativo() {
        ProductoDTO dto = new ProductoDTO(
                "Loratadina 10mg",
                "Antihistamínico",
                30,
                LocalDate.now().plusMonths(6),
                "Otro",
                "LT-2026-008",
                2.50,
                -15.0,
                10
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
                -5,
                LocalDate.now().plusMonths(10),
                "Gastroprotectores",
                "LT-2026-005",
                2.50,
                60.0,
                10
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
                10,
                LocalDate.now().plusMonths(6),
                "Otro",
                "LT-2026-006",
                3.00,
                50.0,
                10
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
                10,
                LocalDate.now().minusDays(1),
                "Otro",
                "LT-2026-007",
                3.00,
                50.0,
                10
        );

        when(repo.existsByNombre("Producto Vencido")).thenReturn(false);

        assertThrows(FechaVencimientoInvalidaException.class, () -> productoService.crear(dto));

        verify(repo, never()).save(any(Producto.class));
    }

    // ═══════════ Fórmula de precio (costo + margen) ═══════════

    @Test
    @DisplayName("Debe calcular el precio de venta correctamente segun el costo y el margen de ganancia")
    void deberiaCalcularPrecioVentaSegunCostoYMargen() {
        double precio1 = productoService.calcularPrecioVenta(2.00, 60.0);
        double precio2 = productoService.calcularPrecioVenta(5.00, 40.0);
        double precio3 = productoService.calcularPrecioVenta(1.50, 0.0);

        assertEquals(3.20, precio1); // 2.00 x 1.60
        assertEquals(7.00, precio2); // 5.00 x 1.40
        assertEquals(1.50, precio3); // 0% de margen = mismo costo
    }

    // ═══════════ Presentaciones de venta ═══════════

    @Test
    @DisplayName("Debe calcular correctamente las unidades a descontar al vender por BLISTER")
    void deberiaCalcularUnidadesAlVenderPorBlister() {
        Producto producto = new Producto();
        producto.setStock(100);
        producto.setUnidadesPorBlister(10);
        producto.setUnidadesPorCaja(100);

        int unidades = productoService.calcularUnidades(producto, Presentacion.BLISTER, 2);

        assertEquals(20, unidades);
    }

    @Test
    @DisplayName("Debe calcular correctamente las unidades a descontar al vender por CAJA")
    void deberiaCalcularUnidadesAlVenderPorCaja() {
        Producto producto = new Producto();
        producto.setStock(500);
        producto.setUnidadesPorBlister(10);
        producto.setUnidadesPorCaja(100);

        int unidades = productoService.calcularUnidades(producto, Presentacion.CAJA, 1);

        assertEquals(100, unidades);
    }

    @Test
    @DisplayName("Debe calcular correctamente las unidades al vender por UNIDAD suelta")
    void deberiaCalcularUnidadesAlVenderPorUnidad() {
        Producto producto = new Producto();
        producto.setStock(50);

        int unidades = productoService.calcularUnidades(producto, Presentacion.UNIDAD, 5);

        assertEquals(5, unidades);
    }

    @Test
    @DisplayName("Debe calcular el precio total correctamente segun la presentacion")
    void deberiaCalcularPrecioTotalSegunPresentacion() {
        Producto producto = new Producto();
        producto.setPrecio(0.50);
        producto.setUnidadesPorBlister(10);
        producto.setUnidadesPorCaja(100);

        double precioBlister = productoService.calcularPrecioTotal(producto, Presentacion.BLISTER, 1);
        double precioCaja = productoService.calcularPrecioTotal(producto, Presentacion.CAJA, 1);
        double precioUnidad = productoService.calcularPrecioTotal(producto, Presentacion.UNIDAD, 3);

        assertEquals(5.0, precioBlister);
        assertEquals(50.0, precioCaja);
        assertEquals(1.5, precioUnidad);
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

        assertEquals(70, resultado.getStock());
        verify(repo, times(1)).save(any(Producto.class));
    }
}