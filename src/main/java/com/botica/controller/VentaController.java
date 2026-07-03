package com.botica.controller;

import com.botica.model.Presentacion;
import com.botica.model.Venta;
import com.botica.service.ProductoService;
import com.botica.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String buscar,
            Model model) {

        Page<Venta> paginaVentas = ventaService.listarPaginado(page, 10, buscar);

        model.addAttribute("ventas", paginaVentas.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", paginaVentas.getTotalPages());
        model.addAttribute("totalElementos", paginaVentas.getTotalElements());
        model.addAttribute("buscar", buscar);

        return "ventas/lista";
    }

    @GetMapping("/nueva")
    public String formulario(Model model) {
        model.addAttribute("venta", new Venta());
        model.addAttribute("productos", productoService.listarTodos());
        return "ventas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Venta venta,
            @RequestParam List<Long> productoIds,
            @RequestParam List<Integer> cantidades,
            @RequestParam(required = false) List<Presentacion> presentaciones,
            RedirectAttributes redirectAttributes) {

        venta.setDetalles(new ArrayList<>());

        // Temporal: mientras el formulario no envíe la presentación,
        // se asume UNIDAD para no romper el flujo actual de ventas.
        if (presentaciones == null || presentaciones.isEmpty()) {
            presentaciones = new ArrayList<>(Collections.nCopies(productoIds.size(), Presentacion.UNIDAD));
        }

        ventaService.registrarVenta(venta, productoIds, cantidades, presentaciones);

        List<String> stockBajo = ventaService.getStockBajoProductos();
        if (!stockBajo.isEmpty()) {
            redirectAttributes.addFlashAttribute("alertaStock",
                    "⚠️ Stock bajo en: " + String.join(", ", stockBajo));
        }

        return "redirect:/ventas";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("venta", ventaService.buscarPorId(id));
        return "ventas/detalle";
    }
}