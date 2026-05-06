package com.botica.controller;

import com.botica.service.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alertas")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @GetMapping
    public String verAlertas(Model model) {

        model.addAttribute("stockBajo",
                alertaService.productosStockBajo());

        model.addAttribute("porVencer",
                alertaService.productosPorVencer());

        model.addAttribute("vencidos",
                alertaService.productosVencidos());

        model.addAttribute("totalAlertas",
                alertaService.totalAlertas());

        return "alertas/lista";
    }
}