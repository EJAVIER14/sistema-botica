package com.botica.controller;

import com.botica.model.Venta;
import com.botica.service.ProductoService;
import com.botica.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", ventaService.listarTodas());
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
            RedirectAttributes redirectAttributes) {

        venta.setDetalles(new ArrayList<>());
        ventaService.registrarVenta(venta, productoIds, cantidades);

        // Verificar stock bajo y mostrar alerta
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