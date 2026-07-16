package com.botica.controller;

import com.botica.model.AvisoMantenimiento;
import com.botica.service.AvisoMantenimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/configuracion/mantenimiento")
public class AvisoMantenimientoController {

    @Autowired
    private AvisoMantenimientoService service;

    @GetMapping
    public String formulario(Model model) {
        AvisoMantenimiento actual = service.obtenerAvisoVigente().orElse(new AvisoMantenimiento());
        model.addAttribute("aviso", actual);
        return "configuracion/mantenimiento";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute AvisoMantenimiento aviso, RedirectAttributes redirectAttributes) {
        service.guardar(aviso);
        redirectAttributes.addFlashAttribute("exito", "Aviso de mantenimiento programado correctamente");
        return "redirect:/configuracion/mantenimiento";
    }

    @GetMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.desactivar(id);
        redirectAttributes.addFlashAttribute("exito", "Aviso de mantenimiento desactivado");
        return "redirect:/configuracion/mantenimiento";
    }
}