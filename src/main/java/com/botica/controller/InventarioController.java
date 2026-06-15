package com.botica.controller;

import com.botica.service.MovimientoInventarioService;
import com.botica.service.OrdenEntradaService;
import com.botica.service.ProductoService;
import com.botica.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    // Ver historial de movimientos
    @GetMapping
    public String verInventario(Model model) {
        model.addAttribute("movimientos", movimientoService.listarTodos());
        model.addAttribute("ordenes", ordenService.listarTodas());
        return "inventario/lista";
    }

    // Abrir formulario nueva orden de entrada
    @GetMapping("/orden/nueva")
    public String formularioOrden(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("proveedores", proveedorService.listarTodos());
        return "inventario/orden-entrada";
    }

    // Guardar orden de entrada
    @PostMapping("/orden/guardar")
    public String guardarOrden(
            @RequestParam Long proveedorId,
            @RequestParam String observacion,
            @RequestParam List<Long> productoIds,
            @RequestParam List<Integer> cantidades,
            RedirectAttributes redirectAttributes) {

        ordenService.crearOrden(proveedorId, observacion, productoIds, cantidades);
        redirectAttributes.addFlashAttribute("exito", "Orden de entrada creada correctamente");
        return "redirect:/inventario";
    }

    // Recibir orden — actualiza stock
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