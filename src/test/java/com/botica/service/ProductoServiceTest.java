package com.botica.service;

import com.botica.dto.ProductoDTO;
import com.botica.exception.PrecioInvalidoException;
import com.botica.exception.ProductoDuplicadoException;
import com.botica.exception.StockInvalidoException;
import com.botica.model.Producto;
import com.botica.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

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
}