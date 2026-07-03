package com.botica.service;

import com.botica.exception.StockInsuficienteException;
import com.botica.model.Presentacion;
import com.botica.model.Producto;
import com.botica.model.Venta;
import com.botica.repository.VentaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoService productoService;

    @Mock
    private MovimientoInventarioService movimientoService;

    @InjectMocks
    private VentaService ventaService;

    @Test
    @DisplayName("Debe registrar una venta por BLISTER calculando el subtotal correctamente")
    void deberiaRegistrarVentaPorBlisterConSubtotalCorrecto() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Paracetamol 500mg");
        producto.setPrecio(0.50);
        producto.setUnidadesPorBlister(10);
        producto.setStock(80); // ya descontado, simulando el resultado de venderPorPresentacion

        when(productoService.buscarPorId(1L)).thenReturn(producto);
        when(productoService.venderPorPresentacion(1L, Presentacion.BLISTER, 2)).thenReturn(producto);
        when(productoService.calcularPrecioTotal(producto, Presentacion.BLISTER, 2)).thenReturn(10.0);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta venta = new Venta();
        venta.setCliente("Cliente Test");
        venta.setDescuento(0.0);
        venta.setMontoRecibido(20.0);

        Venta resultado = ventaService.registrarVenta(
                venta,
                List.of(1L),
                List.of(2),
                List.of(Presentacion.BLISTER)
        );

        assertEquals(1, resultado.getDetalles().size());
        assertEquals(10.0, resultado.getDetalles().get(0).getSubtotal());
        assertEquals(Presentacion.BLISTER, resultado.getDetalles().get(0).getPresentacion());
        verify(movimientoService, times(1)).registrarMovimiento(
                any(Producto.class), eq("SALIDA"), anyInt(), anyInt(), anyInt(), eq("VENTA"), any());
    }

    @Test
    @DisplayName("Debe propagar la excepcion si no hay stock suficiente al vender en una venta")
    void debePropagarExcepcionSiNoHayStockSuficiente() {
        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Ibuprofeno 400mg");
        producto.setPrecio(0.30);
        producto.setStock(5);

        when(productoService.buscarPorId(2L)).thenReturn(producto);
        when(productoService.venderPorPresentacion(2L, Presentacion.CAJA, 1))
                .thenThrow(new StockInsuficienteException("Ibuprofeno 400mg", 5, 100));

        Venta venta = new Venta();
        venta.setCliente("Cliente Test");

        assertThrows(StockInsuficienteException.class, () ->
                ventaService.registrarVenta(venta, List.of(2L), List.of(1), List.of(Presentacion.CAJA)));

        verify(ventaRepository, never()).save(any(Venta.class));
    }
}