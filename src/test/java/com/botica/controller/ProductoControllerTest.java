package com.botica.controller;

import com.botica.dto.ProductoDTO;
import com.botica.model.Producto;
import com.botica.repository.UsuarioRepository;
import com.botica.service.AlertaService;
import com.botica.service.AuditoriaService;
import com.botica.service.AvisoMantenimientoService;
import com.botica.service.ProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private AlertaService alertaService;

    // ═══ NUEVO: requeridos para que el contexto de Spring cargue completo ═══
    // GlobalAvisoMantenimientoAdvice es un @ControllerAdvice que Spring incluye
    // automáticamente en cualquier @WebMvcTest, así que necesita este mock.
    @MockBean
    private AvisoMantenimientoService avisoMantenimientoService;

    // SecurityConfig necesita CustomUserDetailsService, que a su vez necesita esto.
    @MockBean
    private UsuarioRepository usuarioRepository;

    // Los 3 handlers de auditoría (login exitoso/fallido, logout) necesitan esto.
    @MockBean
    private AuditoriaService auditoriaService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /productos debe retornar la vista de lista con los productos")
    void listarDebeRetornarVistaConProductos() throws Exception {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Paracetamol 500mg");

        Page<Producto> pagina = new PageImpl<>(List.of(producto));
        when(productoService.listarPaginado(anyInt(), anyInt(), anyString(), anyString())).thenReturn(pagina);

        // ═══ NUEVO: listar() ahora también llama a listarCategorias() para el dropdown ═══
        when(productoService.listarCategorias()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/lista"))
                .andExpect(model().attributeExists("productos"))
                .andExpect(model().attribute("totalElementos", 1L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /productos/nuevo debe retornar el formulario con un producto vacio")
    void formularioNuevoDebeRetornarFormularioConProductoVacio() throws Exception {
        mockMvc.perform(get("/productos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/formulario"))
                .andExpect(model().attributeExists("producto"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /productos/guardar debe guardar el producto y redirigir a la lista")
    void guardarDebeRedirigirAListaDeProductos() throws Exception {
        Producto productoCreado = new Producto();
        productoCreado.setId(10L);
        productoCreado.setNombre("Ibuprofeno 400mg");

        when(productoService.crear(any(ProductoDTO.class))).thenReturn(productoCreado);

        mockMvc.perform(post("/productos/guardar")
                        .with(csrf())
                        .param("nombre", "Ibuprofeno 400mg")
                        // ═══ ACTUALIZADO: ahora se envían costo + margenGanancia en vez de precio directo ═══
                        .param("costo", "2.50")
                        .param("margenGanancia", "40")
                        .param("stock", "20")
                        .param("categoria", "Analgésico")
                        .param("fechaVencimiento", "2027-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        verify(productoService, times(1)).crear(any(ProductoDTO.class));
        verify(productoService, times(1)).guardar(any(Producto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /productos/eliminar/{id} debe eliminar el producto y redirigir a la lista")
    void eliminarDebeRedirigirAListaDeProductos() throws Exception {
        mockMvc.perform(get("/productos/eliminar/5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        verify(productoService, times(1)).eliminar(5L);
    }
}