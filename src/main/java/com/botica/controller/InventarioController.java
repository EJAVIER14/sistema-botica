package com.botica.controller;

import com.botica.service.MovimientoInventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private MovimientoInventarioService movimientoService;

    @GetMapping
    public String verInventario(Model model) {
        model.addAttribute("movimientos",
                movimientoService.listarTodos());
        return "inventario/lista";
    }
}