package com.botica.controller;

import com.botica.dto.ProductoDTO;
import com.botica.exception.FechaVencimientoInvalidaException;
import com.botica.exception.NombreInvalidoException;
import com.botica.exception.PrecioInvalidoException;
import com.botica.exception.ProductoDuplicadoException;
import com.botica.exception.StockInvalidoException;
import com.botica.model.Producto;
import com.botica.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService service;

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(defaultValue = "") String categoria,
            Model model) {

        Page<Producto> paginaProductos = service.listarPaginado(page, 10, buscar, categoria);

        model.addAttribute("paginaProductos", paginaProductos);
        model.addAttribute("productos", paginaProductos.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", paginaProductos.getTotalPages());
        model.addAttribute("totalElementos", paginaProductos.getTotalElements());
        model.addAttribute("buscar", buscar);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("categorias", service.listarCategorias());

        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Producto producto = service.buscarPorId(id);
        model.addAttribute("producto", producto);
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto, Model model) {
        if (producto.getId() == null) {
            try {
                ProductoDTO dto = new ProductoDTO(
                        producto.getNombre(),
                        producto.getDescripcion(),
                        producto.getPrecio(),
                        producto.getStock(),
                        producto.getFechaVencimiento(),
                        producto.getCategoria(),
                        producto.getLote(),
                        producto.getCosto()
                );

                Producto creado = service.crear(dto);

                creado.setUnidadesPorBlister(producto.getUnidadesPorBlister());
                creado.setUnidadesPorCaja(producto.getUnidadesPorCaja());
                service.guardar(creado);

            } catch (NombreInvalidoException | ProductoDuplicadoException
                     | PrecioInvalidoException | StockInvalidoException
                     | FechaVencimientoInvalidaException e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("producto", producto);
                return "productos/formulario";
            }
        } else {
            service.guardar(producto);
        }

        return "redirect:/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "redirect:/productos";
    }

    @GetMapping("/entrada/{id}")
    public String formularioEntrada(@PathVariable Long id, Model model) {
        model.addAttribute("producto", service.buscarPorId(id));
        return "productos/entrada";
    }

    @PostMapping("/entrada/guardar")
    public String guardarEntrada(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            Authentication authentication) {

        String usuario = authentication.getName();
        service.registrarEntrada(productoId, cantidad, usuario);
        return "redirect:/inventario";
    }

    @GetMapping("/plantilla-excel")
    public ResponseEntity<byte[]> descargarPlantilla() throws Exception {
        byte[] excel = service.generarPlantillaExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_productos.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @PostMapping("/importar-excel")
    public String importarExcel(
            @RequestParam("archivo") MultipartFile archivo,
            RedirectAttributes redirectAttributes) {

        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar un archivo Excel");
            return "redirect:/productos";
        }

        try {
            ProductoService.ResultadoImportacion resultado = service.importarExcel(archivo);

            if (resultado.exitosos > 0) {
                String msg = resultado.exitosos + " productos importados correctamente";
                if (!resultado.errores.isEmpty()) {
                    msg += " (" + resultado.errores.size() + " filas con errores)";
                }
                redirectAttributes.addFlashAttribute("exito", msg);
            } else {
                redirectAttributes.addFlashAttribute("error", "No se importó ningún producto. Verifica el formato del archivo");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al leer el archivo: " + e.getMessage());
        }

        return "redirect:/productos";
    }
}