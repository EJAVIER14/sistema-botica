package com.botica.controller;

import com.botica.model.Venta;
import com.botica.service.ProductoService;
import com.botica.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoService productoService;

    // Ver lista de ventas
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas",
                ventaService.listarTodas());
        return "ventas/lista";
    }

    // Abrir formulario de nueva venta
    @GetMapping("/nueva")
    public String formulario(Model model) {
        model.addAttribute("venta", new Venta());
        model.addAttribute("productos",
                productoService.listarTodos());
        return "ventas/formulario";
    }

    // Guardar la venta
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Venta venta,
            @RequestParam List<Long> productoIds,
            @RequestParam List<Integer> cantidades) {

        venta.setDetalles(new ArrayList<>());
        ventaService.registrarVenta(
                venta, productoIds, cantidades);
        return "redirect:/ventas";
    }

    // Ver detalle de una venta
    @GetMapping("/detalle/{id}")
    public String detalle(
            @PathVariable Long id, Model model) {
        model.addAttribute("venta",
                ventaService.buscarPorId(id));
        return "ventas/detalle";
    }
}