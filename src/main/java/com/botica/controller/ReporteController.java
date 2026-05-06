package com.botica.controller;

import com.botica.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping
    public String verReportes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fin,

            Model model) {

        // Si no hay fechas usa el mes actual
        if (inicio == null) {
            inicio = LocalDate.now()
                    .withDayOfMonth(1);
        }
        if (fin == null) {
            fin = LocalDate.now();
        }

        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);

        model.addAttribute("ventas",
                reporteService.ventasEntreFechas(
                        inicio, fin));

        model.addAttribute("totalIngresos",
                reporteService.totalIngresos(inicio, fin));

        model.addAttribute("cantidadVentas",
                reporteService.cantidadVentas(inicio, fin));

        model.addAttribute("productosMasVendidos",
                reporteService.productosMasVendidos());

        model.addAttribute("totalHoy",
                reporteService.totalHoy());

        model.addAttribute("ventasHoy",
                reporteService.ventasHoy());

        return "reportes/lista";
    }
}