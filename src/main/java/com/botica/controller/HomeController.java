package com.botica.controller;

import com.botica.service.AlertaService;
import com.botica.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private AlertaService alertaService;

    @GetMapping("/")
    public String inicio(Model model) {

        // Resumen del día
        model.addAttribute("totalHoy",
                reporteService.totalHoy());
        model.addAttribute("ventasHoy",
                reporteService.ventasHoy());
        model.addAttribute("stockCritico",
                reporteService.stockCritico());
        model.addAttribute("porVencer",
                reporteService.productosPorVencer());

        // Últimas ventas
        model.addAttribute("ultimasVentas",
                reporteService.ultimasVentas());

        // Alertas
        model.addAttribute("alertasStock",
                alertaService.productosStockBajo());
        model.addAttribute("alertasVencimiento",
                alertaService.productosPorVencer());
        model.addAttribute("alertasVencidos",
                alertaService.productosVencidos());

        return "index";
    }
}