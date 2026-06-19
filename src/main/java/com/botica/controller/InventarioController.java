package com.botica.controller;

import com.botica.model.MovimientoInventario;
import com.botica.service.MovimientoInventarioService;
import com.botica.service.OrdenEntradaService;
import com.botica.service.ProductoService;
import com.botica.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private MovimientoInventarioService movimientoService;

    @Autowired
    private OrdenEntradaService ordenService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    public String verInventario(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<MovimientoInventario> paginaMovimientos = movimientoService.listarPaginado(page, 10);

        model.addAttribute("movimientos", paginaMovimientos.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", paginaMovimientos.getTotalPages());
        model.addAttribute("totalElementos", paginaMovimientos.getTotalElements());

        model.addAttribute("ordenes", ordenService.listarTodas());
        return "inventario/lista";
    }

    @GetMapping("/orden/nueva")
    public String formularioOrden(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("proveedores", proveedorService.listarTodos());
        return "inventario/orden-entrada";
    }

    @PostMapping("/orden/guardar")
    public String guardarOrden(
            @RequestParam Long proveedorId,
            @RequestParam String observacion,
            @RequestParam List<Long> productoIds,
            @RequestParam List<Integer> cantidades,
            @RequestParam(required = false) MultipartFile foto,
            @RequestParam(required = false) MultipartFile documento,
            RedirectAttributes redirectAttributes) {

        try {
            ordenService.crearOrden(proveedorId, observacion, productoIds, cantidades, foto, documento);
            redirectAttributes.addFlashAttribute("exito", "Orden de entrada creada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la orden: " + e.getMessage());
        }
        return "redirect:/inventario";
    }

    @PostMapping("/orden/recibir/{id}")
    public String recibirOrden(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        ordenService.recibirOrden(id, authentication.getName());
        redirectAttributes.addFlashAttribute("exito", "Orden recibida y stock actualizado");
        return "redirect:/inventario";
    }
}